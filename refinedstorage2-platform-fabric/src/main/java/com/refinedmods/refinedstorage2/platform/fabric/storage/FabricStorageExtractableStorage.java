package com.refinedmods.refinedstorage2.platform.fabric.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageExtractableStorage<T, P> implements ExtractableStorage<T> {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<T, P> toPlatformMapper;
    private final Direction direction;
    private final AmountOverride amountOverride;

    public FabricStorageExtractableStorage(final BlockApiLookup<Storage<P>, Direction> lookup,
                                           final Function<T, P> toPlatformMapper,
                                           final ServerLevel serverLevel,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final AmountOverride amountOverride) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.toPlatformMapper = toPlatformMapper;
        this.direction = direction;
        this.amountOverride = amountOverride;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0L;
        }
        final P platformResource = toPlatformMapper.apply(resource);
        final long correctedAmount = amountOverride.overrideAmount(
            resource,
            amount,
            () -> FabricStorageUtil.getCurrentAmount(storage, platformResource)
        );
        if (correctedAmount == 0) {
            return 0;
        }
        return doExtract(resource, correctedAmount, action, storage);
    }

    private long doExtract(final T resource, final long amount, final Action action, final Storage<P> storage) {
        try (Transaction tx = Transaction.openOuter()) {
            final long extract = storage.extract(toPlatformMapper.apply(resource), amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return extract;
        }
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class StorageInsertableStorage<T, P> implements InsertableStorage<T> {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<T, P> toPlatformMapper;
    private final Direction direction;
    private final AmountOverride amountOverride;

    public StorageInsertableStorage(final BlockApiLookup<Storage<P>, Direction> lookup,
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
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0;
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
        return doInsert(platformResource, correctedAmount, action, storage);
    }

    private long doInsert(final P platformResource, final long amount, final Action action, final Storage<P> storage) {
        try (Transaction tx = Transaction.openOuter()) {
            final long inserted = storage.insert(platformResource, amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return inserted;
        }
    }
}

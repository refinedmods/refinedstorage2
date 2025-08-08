package com.refinedmods.refinedstorage.fabric.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageExtractableStorage<P> implements ExtractableStorage {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<ResourceKey, @NullableType P> toPlatformMapper;
    private final Direction direction;

    public FabricStorageExtractableStorage(final BlockApiLookup<Storage<P>, Direction> lookup,
                                           final Function<ResourceKey, @NullableType P> toPlatformMapper,
                                           final ServerLevel serverLevel,
                                           final BlockPos pos,
                                           final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.toPlatformMapper = toPlatformMapper;
        this.direction = direction;
    }

    public long getAmount(final ResourceKey resource) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0L;
        }
        final P platformResource = toPlatformMapper.apply(resource);
        if (platformResource == null) {
            return 0L;
        }
        return FabricStorageUtil.getCurrentAmount(storage, platformResource);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0L;
        }
        final P platformResource = toPlatformMapper.apply(resource);
        if (platformResource == null) {
            return 0L;
        }
        return extract(resource, amount, action, storage);
    }

    @SuppressWarnings("deprecation")
    private long extract(final ResourceKey resource, final long amount, final Action action, final Storage<P> storage) {
        final TransactionContext potentialOpenTransactionFromEarlierInTheStack = Transaction.getCurrentUnsafe();
        try (Transaction tx = Transaction.openNested(potentialOpenTransactionFromEarlierInTheStack)) {
            final long extract = storage.extract(toPlatformMapper.apply(resource), amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return extract;
        }
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

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

    public StorageInsertableStorage(final BlockApiLookup<Storage<P>, Direction> lookup,
                                    final Function<T, P> toPlatformMapper,
                                    final ServerLevel serverLevel,
                                    final BlockPos pos,
                                    final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.toPlatformMapper = toPlatformMapper;
        this.direction = direction;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0L;
        }
        try (Transaction tx = Transaction.openOuter()) {
            final long inserted = storage.insert(toPlatformMapper.apply(resource), amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return inserted;
        }
    }
}

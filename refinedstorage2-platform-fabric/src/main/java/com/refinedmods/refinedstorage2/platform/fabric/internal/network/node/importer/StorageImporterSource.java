package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.StorageInsertableStorage;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;

public class StorageImporterSource<T, P> implements ImporterSource<T> {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<P, T> fromPlatformMapper;
    private final Function<T, P> toPlatformMapper;
    private final StorageInsertableStorage<T, P> insertTarget;
    private final Direction direction;

    public StorageImporterSource(final BlockApiLookup<Storage<P>, Direction> lookup,
                                 final Function<P, T> fromPlatformMapper,
                                 final Function<T, P> toPlatformMapper,
                                 final ServerLevel serverLevel,
                                 final BlockPos pos,
                                 final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
        this.insertTarget = new StorageInsertableStorage<>(lookup, toPlatformMapper, serverLevel, pos, direction);
        this.direction = direction;
    }

    @Override
    public Iterator<T> getResources() {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return Collections.emptyListIterator();
        }
        final Iterator<StorageView<P>> iterator = storage.iterator();
        return transform(
            filter(iterator, storageView -> !storageView.isResourceBlank()),
            storageView -> fromPlatformMapper.apply(storageView.getResource())
        );
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return 0L;
        }
        try (Transaction tx = Transaction.openOuter()) {
            final long extracted = storage.extract(toPlatformMapper.apply(resource), amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}

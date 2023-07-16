package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.StorageExtractableStorage;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.StorageInsertableStorage;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;

public class StorageExternalStorageProvider<T, P> implements ExternalStorageProvider<T> {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<P, T> fromPlatformMapper;
    private final StorageExtractableStorage<T, P> extractTarget;
    private final StorageInsertableStorage<T, P> insertTarget;
    private final Direction direction;

    public StorageExternalStorageProvider(final BlockApiLookup<Storage<P>, Direction> lookup,
                                          final Function<P, T> fromPlatformMapper,
                                          final Function<T, P> toPlatformMapper,
                                          final ServerLevel serverLevel,
                                          final BlockPos pos,
                                          final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.fromPlatformMapper = fromPlatformMapper;
        this.extractTarget = new StorageExtractableStorage<>(lookup, toPlatformMapper, serverLevel, pos, direction);
        this.insertTarget = new StorageInsertableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction,
            AmountOverride.NONE
        );
        this.direction = direction;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return Collections.emptyListIterator();
        }
        final Iterator<StorageView<P>> iterator = storage.iterator();
        return transform(
            filter(iterator, storageView -> !storageView.isResourceBlank()),
            storageView -> new ResourceAmount<>(
                fromPlatformMapper.apply(storageView.getResource()),
                storageView.getAmount()
            )
        );
    }
}

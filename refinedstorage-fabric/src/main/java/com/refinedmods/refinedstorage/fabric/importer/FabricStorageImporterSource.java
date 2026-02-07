package com.refinedmods.refinedstorage.fabric.importer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterSource;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.fabric.storage.FabricStorageExtractableStorage;
import com.refinedmods.refinedstorage.fabric.storage.FabricStorageInsertableStorage;

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
import org.jspecify.annotations.Nullable;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;

class FabricStorageImporterSource<T> implements ImporterSource {
    private final BlockApiCache<Storage<T>, Direction> cache;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final FabricStorageInsertableStorage<T> insertTarget;
    private final FabricStorageExtractableStorage<T> extractTarget;
    private final Direction direction;

    FabricStorageImporterSource(final BlockApiLookup<Storage<T>, Direction> lookup,
                                final Function<T, ResourceKey> fromPlatformMapper,
                                final Function<ResourceKey, @Nullable T> toPlatformMapper,
                                final ServerLevel serverLevel,
                                final BlockPos pos,
                                final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.fromPlatformMapper = fromPlatformMapper;
        this.insertTarget = new FabricStorageInsertableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction
        );
        this.extractTarget = new FabricStorageExtractableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction
        );
        this.direction = direction;
    }

    public long getAmount(final ResourceKey resource) {
        return extractTarget.getAmount(resource);
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        final Storage<T> storage = cache.find(direction);
        if (storage == null) {
            return Collections.emptyListIterator();
        }
        final Iterator<StorageView<T>> iterator = storage.iterator();
        return transform(
            filter(iterator, storageView -> !storageView.isResourceBlank()),
            storageView -> fromPlatformMapper.apply(storageView.getResource())
        );
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}

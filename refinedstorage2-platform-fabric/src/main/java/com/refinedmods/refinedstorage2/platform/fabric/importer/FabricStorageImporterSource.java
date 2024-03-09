package com.refinedmods.refinedstorage2.platform.fabric.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.fabric.storage.FabricStorageExtractableStorage;
import com.refinedmods.refinedstorage2.platform.fabric.storage.FabricStorageInsertableStorage;

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

class FabricStorageImporterSource<T> implements ImporterSource {
    private final BlockApiCache<Storage<T>, Direction> cache;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final FabricStorageInsertableStorage<T> insertTarget;
    private final FabricStorageExtractableStorage<T> extractTarget;
    private final Direction direction;

    FabricStorageImporterSource(final BlockApiLookup<Storage<T>, Direction> lookup,
                                final Function<T, ResourceKey> fromPlatformMapper,
                                final Function<ResourceKey, T> toPlatformMapper,
                                final ServerLevel serverLevel,
                                final BlockPos pos,
                                final Direction direction,
                                final AmountOverride amountOverride) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.fromPlatformMapper = fromPlatformMapper;
        this.insertTarget = new FabricStorageInsertableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction,
            AmountOverride.NONE
        );
        this.extractTarget = new FabricStorageExtractableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction,
            amountOverride
        );
        this.direction = direction;
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

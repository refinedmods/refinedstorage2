package com.refinedmods.refinedstorage2.platform.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
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

class FabricStorageExternalStorageProvider<P> implements ExternalStorageProvider {
    private final BlockApiCache<Storage<P>, Direction> cache;
    private final Function<P, ResourceKey> fromPlatformMapper;
    private final FabricStorageExtractableStorage<P> extractTarget;
    private final FabricStorageInsertableStorage<P> insertTarget;
    private final Direction direction;

    FabricStorageExternalStorageProvider(final BlockApiLookup<Storage<P>, Direction> lookup,
                                         final Function<P, ResourceKey> fromPlatformMapper,
                                         final Function<ResourceKey, P> toPlatformMapper,
                                         final ServerLevel serverLevel,
                                         final BlockPos pos,
                                         final Direction direction) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.fromPlatformMapper = fromPlatformMapper;
        this.extractTarget = new FabricStorageExtractableStorage<>(
            lookup,
            toPlatformMapper,
            serverLevel,
            pos,
            direction,
            AmountOverride.NONE
        );
        this.insertTarget = new FabricStorageInsertableStorage<>(
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
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return extractTarget.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }

    @Override
    public Iterator<ResourceAmount> iterator() {
        final Storage<P> storage = cache.find(direction);
        if (storage == null) {
            return Collections.emptyListIterator();
        }
        final Iterator<StorageView<P>> iterator = storage.iterator();
        return transform(
            filter(iterator, storageView -> !storageView.isResourceBlank() && storageView.getAmount() > 0),
            storageView -> new ResourceAmount(
                fromPlatformMapper.apply(storageView.getResource()),
                storageView.getAmount()
            )
        );
    }
}

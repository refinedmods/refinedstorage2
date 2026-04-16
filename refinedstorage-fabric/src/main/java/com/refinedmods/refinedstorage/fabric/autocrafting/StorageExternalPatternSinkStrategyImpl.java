package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategy;

import java.util.Collection;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

class StorageExternalPatternSinkStrategyImpl<T> implements StorageExternalPatternSinkStrategy {
    private final BlockApiCache<Storage<T>, @Nullable Direction> cache;
    private final Function<ResourceKey, @Nullable T> toPlatformMapper;
    private final Direction direction;

    StorageExternalPatternSinkStrategyImpl(
        final BlockApiLookup<Storage<T>, @Nullable Direction> lookup,
        final Function<ResourceKey, @Nullable T> toPlatformMapper,
        final ServerLevel serverLevel,
        final BlockPos pos,
        final Direction direction
    ) {
        this.cache = BlockApiCache.create(lookup, serverLevel, pos);
        this.toPlatformMapper = toPlatformMapper;
        this.direction = direction;
    }

    @Override
    public ExternalPatternSink.Result accept(final Transaction tx, final Collection<ResourceAmount> resources) {
        boolean anyResourceWasApplicable = false;
        for (final ResourceAmount resourceAmount : resources) {
            final T platformResource = toPlatformMapper.apply(resourceAmount.resource());
            if (platformResource == null) {
                continue;
            }
            anyResourceWasApplicable = true;
            final Storage<T> storage = cache.find(direction);
            if (storage == null) {
                return ExternalPatternSink.Result.SKIPPED;
            }
            if (storage.insert(platformResource, resourceAmount.amount(), tx) != resourceAmount.amount()) {
                return ExternalPatternSink.Result.REJECTED;
            }
        }
        return anyResourceWasApplicable
            ? ExternalPatternSink.Result.ACCEPTED
            : ExternalPatternSink.Result.SKIPPED;
    }

    @Override
    public boolean applies(final ResourceKey resource) {
        return toPlatformMapper.apply(resource) != null;
    }

    @Override
    public boolean isEmpty() {
        final Storage<T> storage = cache.find(direction);
        if (storage == null) {
            return true;
        }
        return StorageUtil.findStoredResource(storage) == null;
    }
}

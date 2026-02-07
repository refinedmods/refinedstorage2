package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategy;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategyFactory;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public class StorageExternalPatternSinkStrategyFactoryImpl<T>
    implements StorageExternalPatternSinkStrategyFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<ResourceKey, @Nullable T> toPlatformMapper;

    public StorageExternalPatternSinkStrategyFactoryImpl(
        final BlockApiLookup<Storage<T>, Direction> lookup,
        final Function<ResourceKey, @Nullable T> toPlatformMapper
    ) {
        this.lookup = lookup;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public StorageExternalPatternSinkStrategy create(final ServerLevel level,
                                                     final BlockPos pos,
                                                     final Direction direction) {
        return new StorageExternalPatternSinkStrategyImpl<>(
            lookup,
            toPlatformMapper,
            level,
            pos,
            direction
        );
    }
}

package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.neoforge.api.ResourceHandlerExternalPatternSinkStrategy;
import com.refinedmods.refinedstorage.neoforge.api.ResourceHandlerExternalPatternSinkStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheResourceHandlerProvider;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public class ResourceHandlerExternalPatternSinkStrategyFactoryImpl<T extends Resource>
    implements ResourceHandlerExternalPatternSinkStrategyFactory {
    private final Function<ResourceKey, Optional<T>> toPlatformMapper;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final BlockCapability<ResourceHandler<T>, @Nullable Direction> capability;

    public ResourceHandlerExternalPatternSinkStrategyFactoryImpl(
        final Function<ResourceKey, Optional<T>> toPlatformMapper,
        final Function<T, ResourceKey> fromPlatformMapper,
        final BlockCapability<ResourceHandler<T>, @Nullable Direction> capability
    ) {
        this.toPlatformMapper = toPlatformMapper;
        this.fromPlatformMapper = fromPlatformMapper;
        this.capability = capability;
    }

    @Override
    public ResourceHandlerExternalPatternSinkStrategy create(final ServerLevel level,
                                                             final BlockPos pos,
                                                             final Direction direction) {
        final CapabilityCacheResourceHandlerProvider<T> provider = new CapabilityCacheResourceHandlerProvider<>(
            level,
            pos,
            direction,
            capability,
            fromPlatformMapper
        );
        return new ResourceHandlerExternalPatternSinkStrategyImpl<>(provider, toPlatformMapper);
    }
}

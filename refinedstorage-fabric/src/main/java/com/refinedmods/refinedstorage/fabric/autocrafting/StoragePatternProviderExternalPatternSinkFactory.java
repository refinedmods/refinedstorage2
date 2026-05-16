package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class StoragePatternProviderExternalPatternSinkFactory implements PatternProviderExternalPatternSinkFactory {
    private final Map<Class<? extends ResourceKey>, StorageExternalPatternSinkStrategyFactory> factories
        = new HashMap<>();

    public void addFactory(final Class<? extends ResourceKey> resourceType,
                           final StorageExternalPatternSinkStrategyFactory factory) {
        factories.put(resourceType, factory);
    }

    @Override
    public PlatformPatternProviderExternalPatternSink create(final ServerLevel level,
                                                             final BlockPos pos,
                                                             final Direction direction) {
        return new StoragePatternProviderExternalPatternSink(factories.entrySet().stream().collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().create(level, pos, direction)
            )));
    }
}

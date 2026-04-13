package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategyFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class StoragePatternProviderExternalPatternSinkFactory implements PatternProviderExternalPatternSinkFactory {
    private final Set<StorageExternalPatternSinkStrategyFactory> factories = new HashSet<>();

    public void addFactory(final StorageExternalPatternSinkStrategyFactory factory) {
        factories.add(factory);
    }

    @Override
    public PlatformPatternProviderExternalPatternSink create(final ServerLevel level,
                                                             final BlockPos pos,
                                                             final Direction direction) {
        return new StoragePatternProviderExternalPatternSink(factories
            .stream()
            .map(factory -> factory.create(level, pos, direction))
            .collect(Collectors.toSet()));
    }
}

package com.refinedmods.refinedstorage.neoforge.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public final class CapabilityCacheResourceHandlerProvider<T extends Resource> implements ResourceHandlerProvider<T> {
    private final BlockCapabilityCache<ResourceHandler<T>, @Nullable Direction> capabilityCache;
    private final Function<T, ResourceKey> mapper;

    public CapabilityCacheResourceHandlerProvider(final ServerLevel level, final BlockPos pos,
                                                  final Direction direction,
                                                  final BlockCapability<ResourceHandler<T>, @Nullable Direction>
                                                      capability,
                                                  final Function<T, ResourceKey> mapper) {
        this.capabilityCache = BlockCapabilityCache.create(capability, level, pos, direction);
        this.mapper = mapper;
    }

    @Override
    public Optional<ResourceHandler<T>> resolve() {
        return Optional.ofNullable(capabilityCache.getCapability());
    }

    @Override
    public Iterator<ResourceKey> iterator() {
        return resolve().map(handler -> (Iterator<ResourceKey>) new AbstractIterator<ResourceKey>() {
            private int index;

            @Nullable
            @Override
            protected ResourceKey computeNext() {
                final int size = handler.size();
                if (index > size) {
                    return endOfData();
                }
                for (; index < size; ++index) {
                    final T resourceAtIndex = handler.getResource(index);
                    if (!resourceAtIndex.isEmpty()) {
                        index++;
                        return mapper.apply(resourceAtIndex);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    @Override
    public Iterator<ResourceAmount> amountIterator() {
        return resolve().map(handler -> (Iterator<ResourceAmount>) new AbstractIterator<ResourceAmount>() {
            private int index;

            @Nullable
            @Override
            protected ResourceAmount computeNext() {
                final int size = handler.size();
                if (index > size) {
                    return endOfData();
                }
                for (; index < size; ++index) {
                    final T resourceAtIndex = handler.getResource(index);
                    if (!resourceAtIndex.isEmpty()) {
                        final long amount = handler.getAmountAsLong(index++);
                        if (amount > 0) {
                            return new ResourceAmount(mapper.apply(resourceAtIndex), amount);
                        }
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }
}

package com.refinedmods.refinedstorage.neoforge.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.AbstractIterator;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public final class StaticResourceHandlerProvider<T extends Resource> implements ResourceHandlerProvider<T> {
    private final ResourceHandler<T> handler;
    private final Optional<ResourceHandler<T>> handlerView;
    private final Function<T, ResourceKey> mapper;

    public StaticResourceHandlerProvider(final ResourceHandler<T> handler,
                                         final Function<T, ResourceKey> mapper) {
        this.handler = handler;
        this.handlerView = Optional.of(handler);
        this.mapper = mapper;
    }

    @Override
    public Optional<ResourceHandler<T>> resolve() {
        return handlerView;
    }

    @Override
    public Iterator<ResourceKey> iterator() {
        return new AbstractIterator<>() {
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
        };
    }

    @Override
    public Iterator<ResourceAmount> amountIterator() {
        return new AbstractIterator<>() {
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
                        index++;
                        return new ResourceAmount(mapper.apply(resourceAtIndex), handler.getAmountAsLong(index));
                    }
                }
                return endOfData();
            }
        };
    }
}

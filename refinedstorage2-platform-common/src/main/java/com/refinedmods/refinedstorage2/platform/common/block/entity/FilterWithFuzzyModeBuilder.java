package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FilteredResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerType;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

public final class FilterWithFuzzyModeBuilder {
    private final ResourceContainer filterContainer;
    @Nullable
    private Runnable listener;
    @Nullable
    private Consumer<Set<TypedTemplate<?>>> uniqueTemplatesAcceptor;
    @Nullable
    private Consumer<List<TypedTemplate<?>>> templatesAcceptor;

    private FilterWithFuzzyModeBuilder(final ResourceContainer filterContainer) {
        this.filterContainer = filterContainer;
    }

    public static FilterWithFuzzyModeBuilder of() {
        return of(9);
    }

    public static FilterWithFuzzyModeBuilder of(final int size) {
        return new FilterWithFuzzyModeBuilder(new ResourceContainer(size, ResourceContainerType.FILTER));
    }

    public static <T> FilterWithFuzzyModeBuilder of(final PlatformStorageChannelType<T> storageChannelType) {
        return new FilterWithFuzzyModeBuilder(new FilteredResourceContainer<>(
            9,
            storageChannelType,
            ResourceContainerType.FILTER
        ));
    }

    public static <T> FilterWithFuzzyModeBuilder of(
        final int size,
        final PlatformStorageChannelType<T> storageChannelType,
        final ResourceContainerType containerType,
        final ToLongFunction<ResourceInstance<?>> maxAmountProvider
    ) {
        return new FilterWithFuzzyModeBuilder(new FilteredResourceContainer<>(
            size,
            storageChannelType,
            containerType,
            maxAmountProvider
        ));
    }

    public FilterWithFuzzyModeBuilder listener(final Runnable newListener) {
        this.listener = newListener;
        return this;
    }

    public FilterWithFuzzyModeBuilder uniqueTemplatesAcceptor(
        final Consumer<Set<TypedTemplate<?>>> newUniqueTemplatesAcceptor
    ) {
        this.uniqueTemplatesAcceptor = newUniqueTemplatesAcceptor;
        return this;
    }

    public FilterWithFuzzyModeBuilder templatesAcceptor(
        final Consumer<List<TypedTemplate<?>>> newTemplatesAcceptor
    ) {
        this.templatesAcceptor = newTemplatesAcceptor;
        return this;
    }

    public FilterWithFuzzyMode build() {
        return new FilterWithFuzzyMode(
            filterContainer,
            listener,
            uniqueTemplatesAcceptor,
            templatesAcceptor
        );
    }
}

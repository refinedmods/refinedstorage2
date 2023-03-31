package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class FilterWithFuzzyModeBuilder {
    private final ResourceFilterContainer filterContainer;
    @Nullable
    private Runnable listener;
    @Nullable
    private Consumer<Set<TypedTemplate<?>>> uniqueTemplatesAcceptor;
    @Nullable
    private Consumer<List<TypedTemplate<?>>> templatesAcceptor;

    private FilterWithFuzzyModeBuilder(final ResourceFilterContainer filterContainer) {
        this.filterContainer = filterContainer;
    }

    public static FilterWithFuzzyModeBuilder of() {
        return of(9, -1);
    }

    public static FilterWithFuzzyModeBuilder of(final int size) {
        return of(size, -1);
    }

    public static FilterWithFuzzyModeBuilder of(final int size, final int maxAmount) {
        return new FilterWithFuzzyModeBuilder(new ResourceFilterContainer(size, maxAmount));
    }

    public static <T> FilterWithFuzzyModeBuilder of(
        final PlatformStorageChannelType<T> storageChannelType
    ) {
        return of(9, storageChannelType);
    }

    public static <T> FilterWithFuzzyModeBuilder of(
        final int size,
        final PlatformStorageChannelType<T> storageChannelType
    ) {
        return of(size, -1, storageChannelType);
    }

    public static <T> FilterWithFuzzyModeBuilder of(
        final int size,
        final int maxAmount,
        final PlatformStorageChannelType<T> storageChannelType
    ) {
        return new FilterWithFuzzyModeBuilder(new FilteredResourceFilterContainer<>(
            size,
            storageChannelType,
            maxAmount
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

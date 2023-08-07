package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerType;

import java.util.Collections;
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

    public static <T> FilterWithFuzzyModeBuilder of(final ResourceFactory<T> resourceFactory) {
        return new FilterWithFuzzyModeBuilder(new ResourceContainer(
            9,
            ResourceContainerType.FILTER,
            resourceFactory,
            Collections.emptySet()
        ));
    }

    public static <T> FilterWithFuzzyModeBuilder of(
        final int size,
        final ResourceFactory<T> resourceFactory,
        final ResourceContainerType containerType,
        final ToLongFunction<ResourceInstance<?>> maxAmountProvider
    ) {
        return new FilterWithFuzzyModeBuilder(new ResourceContainer(
            size,
            containerType,
            maxAmountProvider,
            resourceFactory,
            Collections.emptySet()
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

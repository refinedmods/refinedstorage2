package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import net.minecraft.nbt.CompoundTag;

public final class FilterWithFuzzyMode {
    private static final String TAG_FUZZY_MODE = "fm";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private final ResourceFilterContainer filterContainer;
    private final Runnable listener;
    private final Consumer<Set<Object>> templatesAcceptor;
    private final Consumer<List<Object>> orderedTemplatesAcceptor;

    private boolean fuzzyMode;

    public FilterWithFuzzyMode(final ResourceType resourceType,
                               final Runnable listener,
                               final Consumer<Set<Object>> templatesAcceptor,
                               final Consumer<List<Object>> orderedTemplatesAcceptor) {
        this.filterContainer = new FilteredResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            9,
            this::filterContainerChanged,
            resourceType
        );
        this.listener = listener;
        this.templatesAcceptor = templatesAcceptor;
        this.orderedTemplatesAcceptor = orderedTemplatesAcceptor;
    }

    public FilterWithFuzzyMode(final ResourceType resourceType,
                               final Runnable listener,
                               final Consumer<Set<Object>> templatesAcceptor,
                               final Consumer<List<Object>> orderedTemplatesAcceptor,
                               final int size,
                               final long maxAmount) {
        this.filterContainer = new FilteredResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            size,
            this::filterContainerChanged,
            resourceType,
            maxAmount
        );
        this.listener = listener;
        this.templatesAcceptor = templatesAcceptor;
        this.orderedTemplatesAcceptor = orderedTemplatesAcceptor;
    }

    public FilterWithFuzzyMode(final Runnable listener,
                               final Consumer<Set<Object>> templatesAcceptor,
                               final Consumer<List<Object>> orderedTemplatesAcceptor) {
        this.filterContainer = new ResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            9,
            this::filterContainerChanged
        );
        this.listener = listener;
        this.templatesAcceptor = templatesAcceptor;
        this.orderedTemplatesAcceptor = orderedTemplatesAcceptor;
    }

    private void filterContainerChanged() {
        loadTemplates();
        listener.run();
    }

    public ResourceFilterContainer getFilterContainer() {
        return filterContainer;
    }

    public boolean isFuzzyMode() {
        return fuzzyMode;
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        loadTemplates(); // we need to reload the templates as the normalizer will give different outputs now.
        listener.run();
    }

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_RESOURCE_FILTER)) {
            filterContainer.load(tag.getCompound(TAG_RESOURCE_FILTER));
        }
        if (tag.contains(TAG_FUZZY_MODE)) {
            fuzzyMode = tag.getBoolean(TAG_FUZZY_MODE);
        }
        loadTemplates();
    }

    private void loadTemplates() {
        templatesAcceptor.accept(filterContainer.getUniqueTemplates());
        orderedTemplatesAcceptor.accept(filterContainer.getTemplates());
    }

    public void save(final CompoundTag tag) {
        tag.putBoolean(TAG_FUZZY_MODE, fuzzyMode);
        tag.put(TAG_RESOURCE_FILTER, filterContainer.toTag());
    }

    public UnaryOperator<Object> createNormalizer() {
        return value -> {
            if (!fuzzyMode) {
                return value;
            }
            if (value instanceof FuzzyModeNormalizer<?> normalizer) {
                return normalizer.normalize();
            }
            return value;
        };
    }
}

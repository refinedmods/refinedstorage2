package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public final class FilterWithFuzzyMode {
    private static final String TAG_FUZZY_MODE = "fm";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private final ResourceContainer filterContainer;
    @Nullable
    private final Runnable listener;
    @Nullable
    private final Consumer<Set<TypedTemplate<?>>> uniqueTemplatesAcceptor;
    @Nullable
    private final Consumer<List<TypedTemplate<?>>> templatesAcceptor;

    private boolean fuzzyMode;

    public FilterWithFuzzyMode(final ResourceContainer filterContainer,
                               @Nullable final Runnable listener,
                               @Nullable final Consumer<Set<TypedTemplate<?>>> uniqueTemplatesAcceptor,
                               @Nullable final Consumer<List<TypedTemplate<?>>> templatesAcceptor) {
        this.filterContainer = filterContainer;
        this.listener = listener;
        this.uniqueTemplatesAcceptor = uniqueTemplatesAcceptor;
        this.templatesAcceptor = templatesAcceptor;
        this.filterContainer.setListener(this::filterContainerChanged);
    }

    private void filterContainerChanged() {
        loadTemplates();
        if (listener != null) {
            listener.run();
        }
    }

    public ResourceContainer getFilterContainer() {
        return filterContainer;
    }

    public boolean isFuzzyMode() {
        return fuzzyMode;
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        // We need to reload the templates as the normalizer will give different outputs now.
        loadTemplates();
        if (listener != null) {
            listener.run();
        }
    }

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_RESOURCE_FILTER)) {
            filterContainer.fromTag(tag.getCompound(TAG_RESOURCE_FILTER));
        }
        if (tag.contains(TAG_FUZZY_MODE)) {
            fuzzyMode = tag.getBoolean(TAG_FUZZY_MODE);
        }
        loadTemplates();
    }

    private void loadTemplates() {
        if (uniqueTemplatesAcceptor != null) {
            uniqueTemplatesAcceptor.accept(filterContainer.getUniqueTemplates());
        }
        if (templatesAcceptor != null) {
            templatesAcceptor.accept(filterContainer.getTemplates());
        }
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

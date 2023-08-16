package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;

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
    private final Consumer<Set<Object>> uniqueTemplateListener;
    @Nullable
    private final Consumer<List<ResourceTemplate<?>>> templateListener;

    private boolean fuzzyMode;

    private FilterWithFuzzyMode(final ResourceContainer filterContainer,
                                @Nullable final Runnable listener,
                                @Nullable final Consumer<Set<Object>> uniqueTemplateListener,
                                @Nullable final Consumer<List<ResourceTemplate<?>>> templateListener) {
        this.filterContainer = filterContainer;
        this.listener = listener;
        this.uniqueTemplateListener = uniqueTemplateListener;
        this.templateListener = templateListener;
        this.filterContainer.setListener(this::filterContainerChanged);
    }

    private void filterContainerChanged() {
        notifyListeners();
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
        notifyListeners();
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
        notifyListeners();
    }

    private void notifyListeners() {
        if (uniqueTemplateListener != null) {
            uniqueTemplateListener.accept(filterContainer.getUniqueTemplates());
        }
        if (templateListener != null) {
            templateListener.accept(filterContainer.getTemplates());
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

    public static FilterWithFuzzyMode create(final ResourceContainer resourceContainer,
                                             @Nullable final Runnable listener) {
        return new FilterWithFuzzyMode(resourceContainer, listener, null, null);
    }

    public static FilterWithFuzzyMode createAndListenForTemplates(
        final ResourceContainer resourceContainer,
        final Runnable listener,
        final Consumer<List<ResourceTemplate<?>>> templateListener
    ) {
        return new FilterWithFuzzyMode(resourceContainer, listener, null, templateListener);
    }

    public static FilterWithFuzzyMode createAndListenForUniqueTemplates(
        final ResourceContainer resourceContainer,
        final Runnable listener,
        final Consumer<Set<Object>> templateListener
    ) {
        return new FilterWithFuzzyMode(resourceContainer, listener, templateListener, null);
    }
}

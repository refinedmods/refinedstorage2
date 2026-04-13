package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class FilterWithFuzzyMode {
    private static final String TAG_FUZZY_MODE = "fm";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private final ResourceContainer filterContainer;
    @Nullable
    private final Runnable listener;
    @Nullable
    private final Consumer<Set<ResourceKey>> uniqueFilterListener;
    @Nullable
    private final Consumer<List<ResourceKey>> filterListener;

    private boolean fuzzyMode;

    private FilterWithFuzzyMode(final ResourceContainer filterContainer,
                                @Nullable final Runnable listener,
                                @Nullable final Consumer<Set<ResourceKey>> uniqueFilterListener,
                                @Nullable final Consumer<List<ResourceKey>> filterListener) {
        this.filterContainer = filterContainer;
        this.listener = listener;
        this.uniqueFilterListener = uniqueFilterListener;
        this.filterListener = filterListener;
        this.filterContainer.setListener(() -> notifyListeners(true));
    }

    public ResourceContainer getFilterContainer() {
        return filterContainer;
    }

    public boolean isFuzzyMode() {
        return fuzzyMode;
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        // We need to reload the filters as the normalizer will give different outputs now.
        notifyListeners(true);
    }

    public UnaryOperator<ResourceKey> createNormalizer() {
        return value -> {
            if (!fuzzyMode) {
                return value;
            }
            if (value instanceof FuzzyModeNormalizer normalizer) {
                return normalizer.normalize();
            }
            return value;
        };
    }

    public void store(final ValueOutput output) {
        output.putBoolean(TAG_FUZZY_MODE, fuzzyMode);
        output.store(TAG_RESOURCE_FILTER, ResourceCodecs.CONTAINER_CONTENTS_CODEC,
            ResourceContainerContents.of(filterContainer));
    }

    public void read(final ValueInput input) {
        fuzzyMode = input.getBooleanOr(TAG_FUZZY_MODE, false);
        input.read(TAG_RESOURCE_FILTER, ResourceCodecs.CONTAINER_CONTENTS_CODEC).ifPresent(filterContainer::load);
        notifyListeners(false);
    }

    private void notifyListeners(final boolean changed) {
        if (uniqueFilterListener != null) {
            uniqueFilterListener.accept(filterContainer.getUniqueResources());
        }
        if (filterListener != null) {
            filterListener.accept(filterContainer.getResources());
        }
        if (changed && listener != null) {
            listener.run();
        }
    }

    public static FilterWithFuzzyMode create(
        final ResourceContainer resourceContainer,
        @Nullable final Runnable changeListener
    ) {
        return new FilterWithFuzzyMode(resourceContainer, changeListener, null, null);
    }

    public static FilterWithFuzzyMode createAndListenForFilters(
        final ResourceContainer resourceContainer,
        final Runnable changeListener,
        final Consumer<List<ResourceKey>> listener
    ) {
        return new FilterWithFuzzyMode(resourceContainer, changeListener, null, listener);
    }

    public static FilterWithFuzzyMode createAndListenForUniqueFilters(
        final ResourceContainer resourceContainer,
        final Runnable changeListener,
        final Consumer<Set<ResourceKey>> listener
    ) {
        return new FilterWithFuzzyMode(resourceContainer, changeListener, listener, null);
    }
}

package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class PreviewBuilder {
    private final Map<ResourceKey, MutablePreviewItem> items;
    private List<ResourceAmount> outputsOfPatternWithCycle = Collections.emptyList();
    private boolean missing;
    private final @Nullable PreviewBuilder parent;

    private PreviewBuilder() {
        items = new LinkedHashMap<>();
        parent = null;
    }

    private PreviewBuilder(final PreviewBuilder parent) {
        items = new LinkedHashMap<>();
        this.parent = parent;
        missing = parent.missing;
        outputsOfPatternWithCycle = parent.outputsOfPatternWithCycle;
    }

    public static PreviewBuilder create() {
        return new PreviewBuilder();
    }

    private MutablePreviewItem get(final ResourceKey resource) {
        return items.computeIfAbsent(resource, key -> new MutablePreviewItem());
    }

    public PreviewBuilder withPatternWithCycle(final Pattern pattern) {
        this.outputsOfPatternWithCycle = pattern.layout().outputs();
        return this;
    }

    public PreviewBuilder addAvailable(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "Available amount must be larger than 0");
        get(resource).available += amount;
        return this;
    }

    public PreviewBuilder addMissing(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "Missing amount must be larger than 0");
        get(resource).missing += amount;
        missing = true;
        return this;
    }

    public PreviewBuilder addToCraft(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "To craft amount must be larger than 0");
        get(resource).toCraft += amount;
        return this;
    }

    public PreviewBuilder copy() {
        return new PreviewBuilder(this);
    }

    public Preview build() {
        PreviewBuilder builder = this;
        // collect a trace until the root (excluded)
        final ArrayDeque<PreviewBuilder> path = new ArrayDeque<>();
        while (builder.parent != null) {
            path.addFirst(builder);
            builder = builder.parent;
        }
        // walk from the root downwards to ensure proper ordering of the items
        final PreviewBuilder root = builder;
        for (final PreviewBuilder previewBuilder : path) {
            for (final Map.Entry<ResourceKey, MutablePreviewItem> entry : previewBuilder.items.entrySet()) {
                root.items.merge(entry.getKey(), entry.getValue().copy(), MutablePreviewItem::merge);
            }
        }
        return new Preview(getType(), root.items.entrySet()
            .stream()
            .map(entry -> entry.getValue().toPreviewItem(entry.getKey()))
            .toList(), outputsOfPatternWithCycle);
    }

    private PreviewType getType() {
        if (!outputsOfPatternWithCycle.isEmpty()) {
            return PreviewType.CYCLE_DETECTED;
        }
        return missing ? PreviewType.MISSING_RESOURCES : PreviewType.SUCCESS;
    }

    private static class MutablePreviewItem {
        private long available;
        private long missing;
        private long toCraft;

        private PreviewItem toPreviewItem(final ResourceKey resource) {
            return new PreviewItem(resource, available, missing, toCraft);
        }

        private MutablePreviewItem copy() {
            final MutablePreviewItem copy = new MutablePreviewItem();
            copy.available = available;
            copy.missing = missing;
            copy.toCraft = toCraft;
            return copy;
        }

        private MutablePreviewItem merge(final MutablePreviewItem mutablePreviewItem) {
            available += mutablePreviewItem.available;
            missing += mutablePreviewItem.missing;
            toCraft += mutablePreviewItem.toCraft;
            return this;
        }
    }
}

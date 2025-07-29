package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MutableTreePreviewNode {
    private final ResourceKey resource;
    private final Map<ResourceKey, MutableTreePreviewNode> children = new LinkedHashMap<>();
    private long amount;
    private long toCraft;
    private long available;
    private long missing;

    MutableTreePreviewNode(final ResourceKey resource) {
        this.resource = resource;
    }

    void available(final long amt) {
        this.available += amt;
    }

    void toCraft(final long amt) {
        this.toCraft += amt;
    }

    void missing(final long amt) {
        this.missing += amt;
    }

    void add(final long amt) {
        this.amount += amt;
    }

    MutableTreePreviewNode add(final ResourceKey childResource) {
        return add(childResource, 0);
    }

    MutableTreePreviewNode add(final ResourceKey childResource, final long childAmount) {
        final MutableTreePreviewNode existing = children.get(childResource);
        if (existing != null) {
            existing.add(childAmount);
            return existing;
        }
        final MutableTreePreviewNode child = new MutableTreePreviewNode(childResource);
        child.add(childAmount);
        children.put(childResource, child);
        return child;
    }

    void merge(final MutableTreePreviewNode node) {
        final MutableTreePreviewNode merged = add(node.resource, node.amount);
        merged.available(node.available);
        merged.toCraft(node.toCraft);
        merged.missing(node.missing);
        node.children.values().forEach(merged::merge);
    }

    private boolean hasMissing() {
        return missing > 0 || children.values().stream().anyMatch(MutableTreePreviewNode::hasMissing);
    }

    private PreviewType getType() {
        if (hasMissing()) {
            return PreviewType.MISSING_RESOURCES;
        }
        return PreviewType.SUCCESS;
    }

    TreePreview asRootInPreview() {
        return new TreePreview(getType(), build(), Collections.emptyList());
    }

    TreePreviewNode build() {
        return new TreePreviewNode(
            resource,
            amount,
            toCraft,
            available,
            missing,
            children.values().stream().map(MutableTreePreviewNode::build).toList()
        );
    }

    @Override
    public String toString() {
        return "MutableTreePreviewNode{"
            + "resource=" + resource
            + ", amount=" + amount
            + ", toCraft=" + toCraft
            + ", available=" + available
            + ", missing=" + missing
            + ", children=" + children.values()
            + '}';
    }
}

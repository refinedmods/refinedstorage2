package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TreePreviewNode {
    private final ResourceKey resource;
    private final Map<ResourceKey, TreePreviewNode> children = new LinkedHashMap<>();
    private long amount;
    private long toCraft;
    private long available;
    private long missing;

    public TreePreviewNode(final ResourceKey resource) {
        this.resource = resource;
    }

    public TreePreviewNode(final ResourceKey resource,
                           final long amount,
                           final long toCraft,
                           final long available,
                           final long missing,
                           final Collection<TreePreviewNode> children) {
        this.resource = resource;
        this.amount = amount;
        this.toCraft = toCraft;
        this.available = available;
        this.missing = missing;
        for (final TreePreviewNode child : children) {
            this.children.put(child.getResource(), child);
        }
    }

    public void available(final long amt) {
        this.available += amt;
    }

    public void toCraft(final long amt) {
        this.toCraft += amt;
    }

    public void missing(final long amt) {
        this.missing += amt;
    }

    public void add(final long amt) {
        this.amount += amt;
    }

    public TreePreviewNode add(final ResourceKey childResource) {
        return add(childResource, 0);
    }

    public TreePreviewNode add(final ResourceKey childResource, final long childAmount) {
        final TreePreviewNode existing = children.get(childResource);
        if (existing != null) {
            existing.add(childAmount);
            return existing;
        }
        final TreePreviewNode child = new TreePreviewNode(childResource);
        child.add(childAmount);
        children.put(childResource, child);
        return child;
    }

    public void merge(final TreePreviewNode node) {
        final TreePreviewNode merged = add(node.resource, node.amount);
        merged.available(node.available);
        merged.toCraft(node.toCraft);
        merged.missing(node.missing);
        node.children.values().forEach(merged::merge);
    }

    private boolean hasMissing() {
        return missing > 0 || children.values().stream().anyMatch(TreePreviewNode::hasMissing);
    }

    private PreviewType getType() {
        if (hasMissing()) {
            return PreviewType.MISSING_RESOURCES;
        }
        return PreviewType.SUCCESS;
    }

    public TreePreview asRootInPreview() {
        return new TreePreview(getType(), this, Collections.emptyList());
    }

    public ResourceKey getResource() {
        return resource;
    }

    public Collection<TreePreviewNode> getChildren() {
        return children.values();
    }

    public long getAmount() {
        return amount;
    }

    public long getToCraft() {
        return toCraft;
    }

    public long getAvailable() {
        return available;
    }

    public long getMissing() {
        return missing;
    }

    @Override
    public String toString() {
        return "TreePreviewNode{"
            + "resource=" + resource
            + ", amount=" + amount
            + ", toCraft=" + toCraft
            + ", available=" + available
            + ", missing=" + missing
            + ", children=" + children.values()
            + '}';
    }
}

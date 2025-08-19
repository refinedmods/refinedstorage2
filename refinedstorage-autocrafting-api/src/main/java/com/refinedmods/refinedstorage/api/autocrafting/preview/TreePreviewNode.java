package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-beta.4")
public class TreePreviewNode {
    private final ResourceKey resource;
    private final long amount;
    private final long toCraft;
    private final long available;
    private final long missing;
    private final List<TreePreviewNode> children;

    public TreePreviewNode(final ResourceKey resource,
                           final long amount,
                           final long toCraft,
                           final long available,
                           final long missing,
                           final List<TreePreviewNode> children) {
        this.resource = resource;
        this.amount = amount;
        this.toCraft = toCraft;
        this.available = available;
        this.missing = missing;
        this.children = children;
    }

    public ResourceKey getResource() {
        return resource;
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

    public List<TreePreviewNode> getChildren() {
        return children;
    }
}

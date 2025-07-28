package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import javax.annotation.Nullable;

class TreePreviewBuilder {
    private final TreePreviewNode current;
    @Nullable
    private final TreePreviewBuilder parent;
    private final PreviewType type;

    private TreePreviewBuilder(final PreviewType type, final TreePreviewNode current,
                               @Nullable final TreePreviewBuilder parent) {
        this.type = type;
        this.current = current;
        this.parent = parent;
    }

    static TreePreviewBuilder tree(final PreviewType type, final ResourceKey resource, final long amount) {
        final TreePreviewNode rootNode = new TreePreviewNode(resource);
        rootNode.add(amount);
        rootNode.toCraft(amount);
        return new TreePreviewBuilder(type, rootNode, null);
    }

    public TreePreviewBuilder node(final ResourceKey resource, final long amount) {
        final TreePreviewNode childNode = new TreePreviewNode(resource);
        childNode.add(amount);
        return new TreePreviewBuilder(type, childNode, this);
    }

    public TreePreviewBuilder available(final long amount) {
        current.available(amount);
        return this;
    }

    public TreePreviewBuilder toCraft(final long amount) {
        current.toCraft(amount);
        return this;
    }

    public TreePreviewBuilder missing(final long amount) {
        current.missing(amount);
        return this;
    }

    public TreePreviewBuilder end() {
        if (parent == null) {
            throw new IllegalStateException("Cannot end tree while not in a node.");
        }
        parent.current.merge(current);
        return parent;
    }

    public TreePreview build() {
        if (parent != null) {
            throw new IllegalStateException("Cannot build tree while still in a node.");
        }
        return new TreePreview(type, current, Collections.emptyList());
    }
}

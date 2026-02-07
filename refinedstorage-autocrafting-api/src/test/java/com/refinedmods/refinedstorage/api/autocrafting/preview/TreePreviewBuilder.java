package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

class TreePreviewBuilder {
    private TreePreviewNode current;
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
        final TreePreviewNode rootNode = new TreePreviewNode(resource, amount, amount, 0, 0, Collections.emptyList());
        return new TreePreviewBuilder(type, rootNode, null);
    }

    public TreePreviewBuilder node(final ResourceKey resource, final long amount) {
        final TreePreviewNode childNode = new TreePreviewNode(resource, amount, 0, 0, 0, Collections.emptyList());
        return new TreePreviewBuilder(type, childNode, this);
    }

    public TreePreviewBuilder available(final long amount) {
        current = new TreePreviewNode(current.getResource(), current.getAmount(), current.getToCraft(),
            current.getAvailable() + amount, current.getMissing(), current.getChildren());
        return this;
    }

    public TreePreviewBuilder toCraft(final long amount) {
        current = new TreePreviewNode(current.getResource(), current.getAmount(),
            current.getToCraft() + amount, current.getAvailable(), current.getMissing(), current.getChildren());
        return this;
    }

    public TreePreviewBuilder missing(final long amount) {
        current = new TreePreviewNode(current.getResource(), current.getAmount(), current.getToCraft(),
            current.getAvailable(), current.getMissing() + amount, current.getChildren());
        return this;
    }

    public TreePreviewBuilder end() {
        if (parent == null) {
            throw new IllegalStateException("Cannot end tree while not in a node.");
        }
        final Map<ResourceKey, TreePreviewNode> parentChildren = parent.current.getChildren()
            .stream()
            .collect(Collectors.toMap(TreePreviewNode::getResource, node -> node, (a, b) -> a, LinkedHashMap::new));
        parentChildren.put(current.getResource(), new TreePreviewNode(
            current.getResource(),
            current.getAmount(),
            current.getToCraft(),
            current.getAvailable(),
            current.getMissing(),
            current.getChildren()
        ));
        parent.current = new TreePreviewNode(
            parent.current.getResource(),
            parent.current.getAmount(),
            parent.current.getToCraft(),
            parent.current.getAvailable(),
            parent.current.getMissing(),
            parentChildren.values().stream().toList()
        );
        return parent;
    }

    public TreePreview build() {
        if (parent != null) {
            throw new IllegalStateException("Cannot build tree while still in a node.");
        }
        return new TreePreview(type, current, Collections.emptyList());
    }
}

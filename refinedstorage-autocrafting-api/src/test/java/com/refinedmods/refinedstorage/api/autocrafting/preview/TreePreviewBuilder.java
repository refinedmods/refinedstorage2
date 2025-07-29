package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

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
        current = new TreePreviewNode(current.resource(), current.amount(), current.toCraft(),
            current.available() + amount, current.missing(), current.children());
        return this;
    }

    public TreePreviewBuilder toCraft(final long amount) {
        current = new TreePreviewNode(current.resource(), current.amount(),
            current.toCraft() + amount, current.available(), current.missing(), current.children());
        return this;
    }

    public TreePreviewBuilder missing(final long amount) {
        current = new TreePreviewNode(current.resource(), current.amount(), current.toCraft(),
            current.available(), current.missing() + amount, current.children());
        return this;
    }

    public TreePreviewBuilder end() {
        if (parent == null) {
            throw new IllegalStateException("Cannot end tree while not in a node.");
        }
        final Map<ResourceKey, TreePreviewNode> parentChildren = parent.current.children()
            .stream()
            .collect(Collectors.toMap(TreePreviewNode::resource, node -> node, (a, b) -> a, LinkedHashMap::new));
        parentChildren.put(current.resource(), new TreePreviewNode(
            current.resource(),
            current.amount(),
            current.toCraft(),
            current.available(),
            current.missing(),
            current.children()
        ));
        parent.current = new TreePreviewNode(
            parent.current.resource(),
            parent.current.amount(),
            parent.current.toCraft(),
            parent.current.available(),
            parent.current.missing(),
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

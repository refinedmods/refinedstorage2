package com.refinedmods.refinedstorage2.query.parser.node;

import java.util.List;
import java.util.stream.Collectors;

public record ParenNode(List<Node> nodes) implements Node {
    @Override
    public String toString() {
        return "(" + nodes.stream().map(Node::toString).collect(Collectors.joining(" ")) + ")";
    }
}

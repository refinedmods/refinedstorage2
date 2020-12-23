package com.refinedmods.refinedstorage2.core.query.parser.node;

public class ParenNode implements Node {
    private final Node node;

    public ParenNode(Node node) {
        this.node = node;
    }

    public String toString() {
        return "(" + node + ")";
    }
}

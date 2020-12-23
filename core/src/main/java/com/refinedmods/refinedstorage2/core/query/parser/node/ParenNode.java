package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public class ParenNode implements Node {
    private final Node node;

    public ParenNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public String toString() {
        return "(" + node + ")";
    }

    @Override
    public TokenRange getRange() {
        return node.getRange();
    }
}

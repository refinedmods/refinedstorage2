package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

import java.util.List;
import java.util.stream.Collectors;

public record ParenNode(List<Node> nodes) implements Node {
    @Override
    public String toString() {
        return "(" + nodes.stream().map(Node::toString).collect(Collectors.joining(" ")) + ")";
    }

    @Override
    public TokenRange getRange() {
        if (nodes.size() == 1) {
            return nodes.get(0).getRange();
        }
        return TokenRange.combine(nodes.get(0).getRange(), nodes.get(nodes.size() - 1).getRange());
    }
}

package com.refinedmods.refinedstorage2.core.query.parser.node;

import java.util.List;
import java.util.stream.Collectors;

import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public class ParenNode implements Node {
    private final List<Node> nodes;

    public ParenNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }

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

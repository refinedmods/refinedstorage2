package com.refinedmods.refinedstorage.query.parser.node;

import com.refinedmods.refinedstorage.query.lexer.Token;

public record UnaryOpNode(Node node, Token operator) implements Node {
    @Override
    public String toString() {
        return operator.content() + node;
    }
}

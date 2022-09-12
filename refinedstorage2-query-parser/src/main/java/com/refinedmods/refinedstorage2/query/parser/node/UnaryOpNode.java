package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;

public record UnaryOpNode(Node node, Token operator) implements Node {
    @Override
    public String toString() {
        return operator.content() + node;
    }
}

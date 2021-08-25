package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public record UnaryOpNode(Node node, Token operator) implements Node {
    @Override
    public TokenRange getRange() {
        return TokenRange.combine(operator.position().range(), node.getRange());
    }

    @Override
    public String toString() {
        return operator.content() + node.toString();
    }
}

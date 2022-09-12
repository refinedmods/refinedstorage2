package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;

public record LiteralNode(Token token) implements Node {
    @Override
    public String toString() {
        return token.content();
    }
}


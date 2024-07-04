package com.refinedmods.refinedstorage.query.parser.node;

import com.refinedmods.refinedstorage.query.lexer.Token;

public record LiteralNode(Token token) implements Node {
    @Override
    public String toString() {
        return token.content();
    }
}


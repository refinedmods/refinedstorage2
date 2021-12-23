package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public record LiteralNode(Token token) implements Node {
    @Override
    public String toString() {
        return token.content();
    }

    @Override
    public TokenRange getRange() {
        return token.position().range();
    }
}


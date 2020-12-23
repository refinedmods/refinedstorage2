package com.refinedmods.refinedstorage2.core.query.parser.node;

import com.refinedmods.refinedstorage2.core.query.lexer.Token;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public class LiteralNode implements Node {
    private final Token token;

    public LiteralNode(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.getContent();
    }

    @Override
    public TokenRange getRange() {
        return token.getPosition().getRange();
    }
}


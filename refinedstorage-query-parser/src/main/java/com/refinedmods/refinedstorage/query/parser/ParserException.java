package com.refinedmods.refinedstorage.query.parser;

import com.refinedmods.refinedstorage.query.lexer.Token;

public class ParserException extends RuntimeException {
    private final transient Token token;

    public ParserException(final String message, final Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}

package com.refinedmods.refinedstorage2.core.query.lexer;

public class LexerTokenMapping {
    private final String value;
    private final TokenType type;

    public LexerTokenMapping(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }
}

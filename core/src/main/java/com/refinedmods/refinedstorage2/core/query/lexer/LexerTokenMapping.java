package com.refinedmods.refinedstorage2.core.query.lexer;

public record LexerTokenMapping(String value,
                                TokenType type) {
    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }
}

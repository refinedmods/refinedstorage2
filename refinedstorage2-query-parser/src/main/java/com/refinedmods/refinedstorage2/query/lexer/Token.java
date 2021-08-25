package com.refinedmods.refinedstorage2.query.lexer;

public record Token(String content, TokenType type, TokenPosition position) {
    @Override
    public String toString() {
        return "Token{" +
                "content='" + content + '\'' +
                ", type=" + type +
                ", position=" + position +
                '}';
    }
}

package com.refinedmods.refinedstorage2.query.lexer;

public record Token(String content, TokenType type, TokenPosition position) {
    public String getContent() {
        return content;
    }

    public TokenType getType() {
        return type;
    }

    public TokenPosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Token{" +
                "content='" + content + '\'' +
                ", type=" + type +
                ", position=" + position +
                '}';
    }
}

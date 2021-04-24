package com.refinedmods.refinedstorage2.core.query.lexer;

public class Token {
    private final String content;
    private final TokenType type;
    private final TokenPosition position;

    public Token(String content, TokenType type, TokenPosition position) {
        this.content = content;
        this.type = type;
        this.position = position;
    }

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

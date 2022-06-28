package com.refinedmods.refinedstorage2.query.lexer;

public class SyntaxHighlightedCharacter {
    private final String character;
    private String color;

    public SyntaxHighlightedCharacter(final String text, final String color) {
        this.character = text;
        this.color = color;
    }

    public String getCharacter() {
        return character;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }
}

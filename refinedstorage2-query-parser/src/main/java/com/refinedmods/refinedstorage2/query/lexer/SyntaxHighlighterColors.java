package com.refinedmods.refinedstorage2.query.lexer;

import java.util.EnumMap;
import java.util.Map;

public class SyntaxHighlighterColors {
    public static final SyntaxHighlighterColors DEFAULT_COLORS = new SyntaxHighlighterColors("WHITE")
            .setColor(TokenType.IDENTIFIER, "WHITE")
            .setColor(TokenType.UNARY_OP, "AQUA")
            .setColor(TokenType.BIN_OP, "AQUA")
            .setColor(TokenType.PAREN_OPEN, "YELLOW")
            .setColor(TokenType.PAREN_CLOSE, "YELLOW")
            .setColor(TokenType.INTEGER_NUMBER, "GREEN")
            .setColor(TokenType.FLOATING_NUMBER, "GREEN");

    private final Map<TokenType, String> colors = new EnumMap<>(TokenType.class);
    private final String defaultColor;

    public SyntaxHighlighterColors(String defaultColor) {
        this.defaultColor = defaultColor;
    }

    public SyntaxHighlighterColors setColor(TokenType type, String color) {
        colors.put(type, color);
        return this;
    }

    public String getColor(TokenType type) {
        return colors.getOrDefault(type, defaultColor);
    }

    public String getDefaultColor() {
        return defaultColor;
    }
}

package com.refinedmods.refinedstorage2.query.lexer;

import java.util.ArrayList;
import java.util.List;

public class SyntaxHighlighter {
    private final SyntaxHighlighterColors colors;

    public SyntaxHighlighter(final SyntaxHighlighterColors colors) {
        this.colors = colors;
    }

    public List<SyntaxHighlightedCharacter> highlight(final String text, final List<Token> tokens) {
        final List<SyntaxHighlightedCharacter> characters = createInitialCharacters(text);

        for (final Token token : tokens) {
            final int begin = token.position().range().startColumn() - 1;
            final int end = token.position().range().endColumn();

            for (int i = begin; i < end; ++i) {
                characters.get(i).setColor(colors.getColor(token.type()));
            }
        }

        return characters;
    }

    private List<SyntaxHighlightedCharacter> createInitialCharacters(final String text) {
        final List<SyntaxHighlightedCharacter> items = new ArrayList<>();
        for (final char c : text.toCharArray()) {
            items.add(new SyntaxHighlightedCharacter(String.valueOf(c), colors.getDefaultColor()));
        }
        return items;
    }
}

package com.refinedmods.refinedstorage2.core.query.lexer;

import java.util.ArrayList;
import java.util.List;

public class SyntaxHighlighter {
    private final SyntaxHighlighterColors colors;

    public SyntaxHighlighter(SyntaxHighlighterColors colors) {
        this.colors = colors;
    }

    public List<SyntaxHighlightedCharacter> highlight(String text, List<Token> tokens) {
        List<SyntaxHighlightedCharacter> characters = createInitialCharacters(text);

        for (Token token : tokens) {
            int begin = token.getPosition().getRange().getStartColumn() - 1;
            int end = token.getPosition().getRange().getEndColumn();

            for (int i = begin; i < end; ++i) {
                characters.get(i).setColor(colors.getColor(token.getType()));
            }
        }

        return characters;
    }

    private List<SyntaxHighlightedCharacter> createInitialCharacters(String text) {
        List<SyntaxHighlightedCharacter> items = new ArrayList<>();
        for (char c : text.toCharArray()) {
            items.add(new SyntaxHighlightedCharacter(String.valueOf(c), colors.getDefaultColor()));
        }
        return items;
    }
}

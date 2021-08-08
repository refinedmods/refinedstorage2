package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.core.query.lexer.Lexer;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerException;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.lexer.Source;
import com.refinedmods.refinedstorage2.core.query.lexer.SyntaxHighlightedCharacter;
import com.refinedmods.refinedstorage2.core.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.core.util.History;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SearchFieldWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class GridSearchBoxWidget extends SearchFieldWidget implements GridSearchBox {
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    private boolean invalid;

    public GridSearchBoxWidget(TextRenderer textRenderer, int x, int y, int width, SyntaxHighlighter syntaxHighlighter) {
        super(textRenderer, x, y, width, new History(SEARCH_FIELD_HISTORY));

        setRenderTextProvider((text, firstCharacterIndex) -> {
            if (invalid) {
                return invalidText(text);
            }

            Lexer lexer = createLexer(text);
            try {
                lexer.scan();
            } catch (LexerException e) {
                return invalidText(text);
            }

            List<SyntaxHighlightedCharacter> characters = syntaxHighlighter.highlight(text, lexer.getTokens());
            return convertCharactersToOrderedText(characters);
        });
    }

    private OrderedText invalidText(String text) {
        return OrderedText.styledForwardsVisitedString(text, Style.EMPTY.withFormatting(Formatting.RED));
    }

    private OrderedText convertCharactersToOrderedText(List<SyntaxHighlightedCharacter> characters) {
        OrderedText orderedText = OrderedText.EMPTY;
        for (SyntaxHighlightedCharacter character : characters) {
            orderedText = OrderedText.concat(orderedText, convertCharacterToOrderedText(character));
        }
        return orderedText;
    }

    private OrderedText convertCharacterToOrderedText(SyntaxHighlightedCharacter character) {
        Formatting color = Formatting.byName(character.getColor());
        return OrderedText.styledForwardsVisitedString(character.getCharacter(), Style.EMPTY.withColor(color));
    }

    private Lexer createLexer(String text) {
        return new Lexer(new Source("Grid search box syntax highlighting", text), LexerTokenMappings.DEFAULT_MAPPINGS);
    }

    @Override
    public void setAutoSelected(boolean autoSelected) {
        setFocused(autoSelected);
        setFocusUnlocked(!autoSelected);
    }

    @Override
    public void setListener(Consumer<String> listener) {
        setChangedListener(listener);
    }

    @Override
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
        setEditableColor(invalid ? Formatting.RED.getColorValue() : Formatting.WHITE.getColorValue());
    }
}

package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.core.History;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage2.query.lexer.Lexer;
import com.refinedmods.refinedstorage2.query.lexer.LexerException;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.lexer.Source;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlightedCharacter;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class GridSearchBoxWidget extends SearchFieldWidget implements GridSearchBox {
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    private final GridView<?> view;
    private final GridQueryParser queryParser;

    private boolean valid = true;

    public GridSearchBoxWidget(Font textRenderer, int x, int y, int width, SyntaxHighlighter syntaxHighlighter, GridView<?> view, GridQueryParser queryParser) {
        super(textRenderer, x, y, width, new History(SEARCH_FIELD_HISTORY));

        setFormatter((text, firstCharacterIndex) -> {
            if (!valid) {
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

        setListener(text -> {
        });

        this.view = view;
        this.queryParser = queryParser;
    }

    private FormattedCharSequence invalidText(String text) {
        return FormattedCharSequence.forward(text, Style.EMPTY.applyFormat(ChatFormatting.RED));
    }

    private FormattedCharSequence convertCharactersToOrderedText(List<SyntaxHighlightedCharacter> characters) {
        FormattedCharSequence orderedText = FormattedCharSequence.EMPTY;
        for (SyntaxHighlightedCharacter character : characters) {
            orderedText = FormattedCharSequence.composite(orderedText, convertCharacterToOrderedText(character));
        }
        return orderedText;
    }

    private FormattedCharSequence convertCharacterToOrderedText(SyntaxHighlightedCharacter character) {
        ChatFormatting color = ChatFormatting.getByName(character.getColor());
        return FormattedCharSequence.forward(character.getCharacter(), Style.EMPTY.withColor(color));
    }

    private Lexer createLexer(String text) {
        return new Lexer(new Source("Grid search box syntax highlighting", text), LexerTokenMappings.DEFAULT_MAPPINGS);
    }

    @Override
    public void setAutoSelected(boolean autoSelected) {
        setFocused(autoSelected);
        setCanLoseFocus(!autoSelected);
    }

    private boolean onTextChanged(String text) {
        boolean success = true;
        try {
            view.setFilter((Predicate) queryParser.parse(text));
        } catch (GridQueryParserException e) {
            view.setFilter(resource -> false);
            success = false;
        }
        view.sort();
        return success;
    }

    @Override
    public void setListener(Consumer<String> listener) {
        setResponder(text -> {
            listener.accept(text);
            setValid(onTextChanged(text));
        });
    }

    private void setValid(boolean valid) {
        this.valid = valid;
        setTextColor(valid ? ChatFormatting.WHITE.getColor() : ChatFormatting.RED.getColor());
    }
}

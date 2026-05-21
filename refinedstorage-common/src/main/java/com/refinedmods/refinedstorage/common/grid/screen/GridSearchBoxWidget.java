package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.GridSearchBox;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.query.lexer.Lexer;
import com.refinedmods.refinedstorage.query.lexer.LexerException;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.lexer.Source;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlightedCharacter;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

class GridSearchBoxWidget extends SearchFieldWidget implements GridSearchBox {
    private final Set<Consumer<String>> listeners = new HashSet<>();
    private boolean valid = true;

    GridSearchBoxWidget(final Font textRenderer,
                        final int x,
                        final int y,
                        final int width,
                        final SyntaxHighlighter syntaxHighlighter,
                        final History history) {
        super(textRenderer, x, y, width, history);
        addFormatter((text, offset) -> format(syntaxHighlighter, text, offset).getVisualOrderText());
        setResponder(text -> listeners.forEach(l -> l.accept(text)));
    }

    private Component format(final SyntaxHighlighter syntaxHighlighter, final String text, final int offset) {
        final String actualText = getValue();
        final int startIndex = offset;
        final int endIndex = Math.min(actualText.length(), offset + text.length());
        if (!valid) {
            return invalidText(text);
        }
        final Lexer lexer = createLexer(actualText);
        try {
            lexer.scan();
        } catch (LexerException e) {
            return invalidText(text);
        }
        final List<SyntaxHighlightedCharacter> characters = syntaxHighlighter.highlight(actualText, lexer.getTokens());
        if (startIndex == 0 && endIndex == actualText.length()) {
            return toComponent(characters);
        }
        return toComponent(characters.subList(startIndex, endIndex));
    }

    private Component invalidText(final String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }

    private MutableComponent toComponent(final List<SyntaxHighlightedCharacter> characters) {
        final MutableComponent finalComponent = Component.empty().copy();
        for (final SyntaxHighlightedCharacter character : characters) {
            finalComponent.append(toComponent(character));
        }
        return finalComponent;
    }

    private Component toComponent(final SyntaxHighlightedCharacter character) {
        final ChatFormatting color = ChatFormatting.getByName(character.getColor());
        return Component.literal(character.getCharacter()).withStyle(color == null ? ChatFormatting.WHITE : color);
    }

    private Lexer createLexer(final String text) {
        return new Lexer(new Source("Grid search box syntax highlighting", text), LexerTokenMappings.DEFAULT_MAPPINGS);
    }

    @Override
    public void addListener(final Consumer<String> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void setValid(final boolean valid) {
        this.valid = valid;
        setTextColor(valid ? 0xFFFFFFFF : 0xFFFF5555);
    }
}

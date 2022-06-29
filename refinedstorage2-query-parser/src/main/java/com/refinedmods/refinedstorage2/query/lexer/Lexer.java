package com.refinedmods.refinedstorage2.query.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class Lexer {
    private final Source source;
    private final List<Token> tokens = new ArrayList<>();
    private final LexerPosition position = new LexerPosition();
    private final LexerTokenMappings tokenMappings;

    public Lexer(final Source source, final LexerTokenMappings tokenMappings) {
        this.source = source;
        this.tokenMappings = tokenMappings;
    }

    public void scan() {
        while (isNotEof()) {
            final char current = current();

            if (current == '\r') {
                position.advanceAndReset();
            } else if (current == '\n') {
                position.advance();
                position.nextLine();
                position.reset();
            } else if (current == ' ') {
                position.advanceAndReset();
            } else if (tokenMappings.hasMapping(position, source)) {
                addToken(Objects.requireNonNull(tokenMappings.findMapping(position, source)));
            } else if (Character.isDigit(current)) {
                scanNumber();
            } else if (current == '"') {
                scanString();
            } else if (isValidIdentifier(current)) {
                scanIdentifier();
            } else {
                position.advance();
                throw new LexerException(position.createRange(), "Unexpected '" + current + "'");
            }
        }
    }

    private boolean isValidIdentifier(final char c) {
        return Character.isLetterOrDigit(c);
    }

    private void scanNumber() {
        while (isNotEof() && Character.isDigit(current())) {
            position.advance();
        }

        if (isNotEof() && current() == '.') {
            position.advance();

            if (!isNotEof()) {
                throw new LexerException(position.createRange(), "Unexpected end of number");
            }

            if (!Character.isDigit(current())) {
                throw new LexerException(position.createRange(), "Invalid floating point number");
            }

            while (isNotEof() && Character.isDigit(current())) {
                position.advance();
            }

            addToken(TokenType.FLOATING_NUMBER);
        } else {
            addToken(TokenType.INTEGER_NUMBER);
        }
    }

    private void scanIdentifier() {
        while (isNotEof() && isValidIdentifier(current())) {
            position.advance();
        }
        addToken(TokenType.IDENTIFIER);
    }

    private void scanString() {
        position.advance();

        while (isNotEof() && current() != '"') {
            position.advance();
        }

        if (!isNotEof()) {
            throw new LexerException(position.createRange(), "Unexpected end of string");
        }

        position.advance();

        addToken(TokenType.IDENTIFIER, content -> content.substring(1, content.length() - 1));
    }

    private boolean isNotEof() {
        return position.getEndIndex() < source.content().length();
    }

    private char current() {
        return source.content().charAt(position.getEndIndex());
    }

    private void addToken(final TokenType type) {
        addToken(type, content -> content);
    }

    private void addToken(final TokenType type, final UnaryOperator<String> contentModifier) {
        final String tokenContent = contentModifier.apply(
                source.content().substring(position.getStartIndex(), position.getEndIndex())
        );
        final TokenPosition tokenPosition = new TokenPosition(source, position.createRange());

        tokens.add(new Token(tokenContent, type, tokenPosition));

        this.position.reset();
    }

    public List<Token> getTokens() {
        return tokens;
    }
}

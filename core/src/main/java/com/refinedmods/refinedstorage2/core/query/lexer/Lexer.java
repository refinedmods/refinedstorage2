package com.refinedmods.refinedstorage2.core.query.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class Lexer {
    private final Source source;
    private final List<Token> tokens = new ArrayList<>();
    private final LexerPosition position = new LexerPosition();
    private final Map<String, TokenType> tokenMappings = new TreeMap<>((a, b) -> {
        int cmp = Integer.compare(b.length(), a.length());
        if (cmp == 0) {
            return b.compareTo(a);
        }
        return cmp;
    });

    public Lexer(Source source) {
        this.source = source;
    }

    public void registerTokenMapping(String value, TokenType type) {
        tokenMappings.put(value, type);
    }

    public void scan() {
        while (isNotEof()) {
            char current = current();

            TokenType mapping;
            if (current == '\r') {
                position.advanceAndReset();
            } else if (current == '\n') {
                position.advance();
                position.nextLine();
                position.reset();
            } else if (current == ' ') {
                position.advanceAndReset();
            } else if ((mapping = checkTokenMappings()) != null) {
                addToken(mapping);
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

    private boolean isValidIdentifier(char c) {
        return Character.isLetterOrDigit(c);
    }

    private TokenType checkTokenMappings() {
        for (Map.Entry<String, TokenType> entry : this.tokenMappings.entrySet()) {
            TokenType type = entry.getValue();
            String content = entry.getKey();
            int contentLength = content.length();

            if ((position.getEndIndex() + contentLength <= source.getContent().length()) &&
                (content.equals(source.getContent().substring(position.getEndIndex(), position.getEndIndex() + contentLength)))) {
                position.advance(contentLength);

                return type;
            }
        }

        return null;
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
        return position.getEndIndex() < source.getContent().length();
    }

    private char current() {
        return source.getContent().charAt(position.getEndIndex());
    }

    private void addToken(TokenType type) {
        addToken(type, content -> content);
    }

    private void addToken(TokenType type, Function<String, String> contentModifier) {
        String tokenContent = source.getContent().substring(position.getStartIndex(), position.getEndIndex());
        tokenContent = contentModifier.apply(tokenContent);

        TokenPosition tokenPosition = new TokenPosition(source, position.createRange());

        tokens.add(new Token(tokenContent, type, tokenPosition));

        this.position.reset();
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
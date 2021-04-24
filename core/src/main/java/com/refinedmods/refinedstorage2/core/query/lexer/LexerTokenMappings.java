package com.refinedmods.refinedstorage2.core.query.lexer;

import java.util.Set;
import java.util.TreeSet;

public class LexerTokenMappings {
    public static final LexerTokenMappings DEFAULT_MAPPINGS = new LexerTokenMappings()
            .addMapping(new LexerTokenMapping("!", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("@", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("$", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping(">", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping(">=", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("<", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("<=", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("=", TokenType.UNARY_OP))
            .addMapping(new LexerTokenMapping("&&", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("||", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("(", TokenType.PAREN_OPEN))
            .addMapping(new LexerTokenMapping(")", TokenType.PAREN_CLOSE));

    private final Set<LexerTokenMapping> mappings = new TreeSet<>((a, b) -> {
        int cmp = Integer.compare(b.getValue().length(), a.getValue().length());
        if (cmp == 0) {
            return b.getValue().compareTo(a.getValue());
        }
        return cmp;
    });

    public LexerTokenMappings addMapping(LexerTokenMapping mapping) {
        mappings.add(mapping);
        return this;
    }

    public TokenType findMapping(LexerPosition position, Source source) {
        for (LexerTokenMapping mapping : mappings) {
            String content = mapping.getValue();
            int contentLength = mapping.getValue().length();

            if ((position.getEndIndex() + contentLength <= source.getContent().length()) &&
                    (content.equals(source.getContent().substring(position.getEndIndex(), position.getEndIndex() + contentLength)))) {
                position.advance(contentLength);

                return mapping.getType();
            }
        }

        return null;
    }
}

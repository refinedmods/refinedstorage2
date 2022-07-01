package com.refinedmods.refinedstorage2.query.lexer;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.TreeSet;

public class LexerTokenMappings {
    public static final LexerTokenMappings DEFAULT_MAPPINGS = new LexerTokenMappings()
        .addMapping(new LexerTokenMapping("!", TokenType.UNARY_OP))
        .addMapping(new LexerTokenMapping("@", TokenType.UNARY_OP))
        .addMapping(new LexerTokenMapping("$", TokenType.UNARY_OP))
        .addMapping(new LexerTokenMapping("#", TokenType.UNARY_OP))
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
        final int cmp = Integer.compare(b.value().length(), a.value().length());
        if (cmp == 0) {
            return b.value().compareTo(a.value());
        }
        return cmp;
    });

    public LexerTokenMappings addMapping(final LexerTokenMapping mapping) {
        mappings.add(mapping);
        return this;
    }

    public boolean hasMapping(final LexerPosition position, final Source source) {
        return findMapping(position, source) != null;
    }

    @Nullable
    public LexerTokenMapping findMapping(final LexerPosition position, final Source source) {
        for (final LexerTokenMapping mapping : mappings) {
            final String content = mapping.value();
            final int contentLength = mapping.value().length();
            final boolean mappingIsInBounds = position.getEndIndex() + contentLength <= source.content().length();
            if (mappingIsInBounds) {
                final String sourceContent =
                    source.content().substring(position.getEndIndex(), position.getEndIndex() + contentLength);
                final boolean sourceContainsMapping = content.equals(sourceContent);
                if (sourceContainsMapping) {
                    return mapping;
                }
            }
        }
        return null;
    }
}

package com.refinedmods.refinedstorage2.query.lexer;

import static org.assertj.core.api.Assertions.assertThat;

class TokenAssertions {
    private TokenAssertions() {
    }

    public static void assertRange(final TokenRange range,
                                   final int startLine,
                                   final int startColumn,
                                   final int endLine,
                                   final int endColumn) {
        assertThat(range.startLine()).isEqualTo(startLine);
        assertThat(range.startColumn()).isEqualTo(startColumn);
        assertThat(range.endLine()).isEqualTo(endLine);
        assertThat(range.endColumn()).isEqualTo(endColumn);
    }

    public static void assertPosition(final TokenPosition position,
                                      final String sourceName,
                                      final int startLine,
                                      final int startColumn,
                                      final int endLine,
                                      final int endColumn) {
        assertThat(position.source().name()).isEqualTo(sourceName);
        assertRange(position.range(), startLine, startColumn, endLine, endColumn);
    }

    public static void assertToken(final Token token,
                                   final String content,
                                   final TokenType type) {
        assertThat(token.content()).isEqualTo(content);
        assertThat(token.type()).isEqualTo(type);
    }
}

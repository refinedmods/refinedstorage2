package com.refinedmods.refinedstorage2.query.lexer;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenAssertions {
    public static void assertRange(TokenRange range, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(range.startLine()).isEqualTo(startLine);
        assertThat(range.startColumn()).isEqualTo(startColumn);
        assertThat(range.endLine()).isEqualTo(endLine);
        assertThat(range.endColumn()).isEqualTo(endColumn);
    }

    public static void assertPosition(TokenPosition position, String sourceName, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(position.source().name()).isEqualTo(sourceName);
        assertRange(position.range(), startLine, startColumn, endLine, endColumn);
    }

    public static void assertToken(Token token, String content, TokenType type) {
        assertThat(token.content()).isEqualTo(content);
        assertThat(token.type()).isEqualTo(type);
    }
}

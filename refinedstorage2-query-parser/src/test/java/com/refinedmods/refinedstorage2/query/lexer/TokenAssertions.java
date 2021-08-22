package com.refinedmods.refinedstorage2.query.lexer;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenAssertions {
    public static void assertRange(TokenRange range, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(range.getStartLine()).isEqualTo(startLine);
        assertThat(range.getStartColumn()).isEqualTo(startColumn);
        assertThat(range.getEndLine()).isEqualTo(endLine);
        assertThat(range.getEndColumn()).isEqualTo(endColumn);
    }

    public static void assertPosition(TokenPosition position, String sourceName, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(position.getSource().getName()).isEqualTo(sourceName);
        assertRange(position.getRange(), startLine, startColumn, endLine, endColumn);
    }

    public static void assertToken(Token token, String content, TokenType type) {
        assertThat(token.getContent()).isEqualTo(content);
        assertThat(token.getType()).isEqualTo(type);
    }
}

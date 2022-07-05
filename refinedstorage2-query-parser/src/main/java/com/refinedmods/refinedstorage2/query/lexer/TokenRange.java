package com.refinedmods.refinedstorage2.query.lexer;

public record TokenRange(int startLine, int startColumn, int endLine, int endColumn) {
    public static TokenRange combine(final TokenRange low, final TokenRange high) {
        return new TokenRange(low.startLine, low.startColumn, high.endLine, high.endColumn);
    }

    @Override
    public String toString() {
        return startLine + ":" + startColumn + " - " + endLine + ":" + endColumn;
    }
}

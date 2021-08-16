package com.refinedmods.refinedstorage2.core.query.lexer;

public record TokenRange(int startLine, int startColumn, int endLine, int endColumn) {
    public static TokenRange combine(TokenRange low, TokenRange high) {
        return new TokenRange(low.startLine, low.startColumn, high.endLine, high.endColumn);
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String toString() {
        return startLine + ":" + startColumn + " - " + endLine + ":" + endColumn;
    }
}

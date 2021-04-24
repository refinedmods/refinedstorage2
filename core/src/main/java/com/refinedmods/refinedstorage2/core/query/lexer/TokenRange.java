package com.refinedmods.refinedstorage2.core.query.lexer;

public class TokenRange {
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public TokenRange(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

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

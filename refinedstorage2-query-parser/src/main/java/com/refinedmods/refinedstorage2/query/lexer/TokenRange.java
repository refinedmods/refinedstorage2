package com.refinedmods.refinedstorage2.query.lexer;

public record TokenRange(int startLine, int startColumn, int endLine, int endColumn) {
    @Override
    public String toString() {
        return startLine + ":" + startColumn + " - " + endLine + ":" + endColumn;
    }
}

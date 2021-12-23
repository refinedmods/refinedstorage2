package com.refinedmods.refinedstorage2.query.lexer;

public class LexerPosition {
    private int startIndex = 0;
    private int endIndex = 0;

    private int startLine = 1;
    private int endLine = 1;

    private int startColumn = 1;
    private int endColumn = 1;

    public void nextLine() {
        endLine++;
        endColumn = 1;
    }

    public void advance() {
        advance(1);
    }

    public void advance(int n) {
        endIndex += n;
        endColumn += n;
    }

    public void reset() {
        startIndex = endIndex;
        startLine = endLine;
        startColumn = endColumn;
    }

    public void advanceAndReset() {
        advance();
        reset();
    }

    public TokenRange createRange() {
        return new TokenRange(startLine, startColumn, endLine, endColumn - 1);
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}

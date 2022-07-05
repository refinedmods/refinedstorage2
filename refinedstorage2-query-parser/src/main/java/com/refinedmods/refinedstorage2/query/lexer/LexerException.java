package com.refinedmods.refinedstorage2.query.lexer;

public class LexerException extends RuntimeException {
    private final transient TokenRange range;

    public LexerException(final TokenRange range, final String message) {
        super(message);

        this.range = range;
    }

    public TokenRange getRange() {
        return range;
    }
}

package com.refinedmods.refinedstorage2.query.lexer;

public class LexerException extends RuntimeException {
    private final transient TokenRange range;

    public LexerException(TokenRange range, String message) {
        super(message);

        this.range = range;
    }

    public TokenRange getRange() {
        return range;
    }
}

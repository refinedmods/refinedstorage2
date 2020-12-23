package com.refinedmods.refinedstorage2.core.grid.query;

import com.refinedmods.refinedstorage2.core.query.lexer.TokenRange;

public class GridQueryParserException extends Exception {
    private final TokenRange range;

    public GridQueryParserException(TokenRange range, String message, Throwable cause) {
        super(message, cause);

        this.range = range;
    }

    public TokenRange getRange() {
        return range;
    }
}

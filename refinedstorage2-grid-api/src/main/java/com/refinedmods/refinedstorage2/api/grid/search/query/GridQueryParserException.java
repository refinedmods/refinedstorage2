package com.refinedmods.refinedstorage2.api.grid.search.query;

import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridQueryParserException extends Exception {
    private final transient TokenRange range;

    public GridQueryParserException(TokenRange range, String message, Throwable cause) {
        super(message, cause);

        this.range = range;
    }

    public TokenRange getRange() {
        return range;
    }
}

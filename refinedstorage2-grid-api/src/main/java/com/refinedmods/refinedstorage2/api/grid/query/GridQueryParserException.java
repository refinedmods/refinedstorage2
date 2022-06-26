package com.refinedmods.refinedstorage2.api.grid.query;

import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridQueryParserException extends Exception {
    private final transient TokenRange range;

    public GridQueryParserException(final TokenRange range, final String message, @Nullable final Throwable cause) {
        super(message, cause);
        this.range = range;
    }

    public TokenRange getRange() {
        return range;
    }
}

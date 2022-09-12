package com.refinedmods.refinedstorage2.api.grid.query;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridQueryParserException extends Exception {
    public GridQueryParserException(final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}

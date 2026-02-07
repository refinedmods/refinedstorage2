package com.refinedmods.refinedstorage.common.grid.query;

import org.jspecify.annotations.Nullable;

public class GridQueryParserException extends Exception {
    public GridQueryParserException(final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}

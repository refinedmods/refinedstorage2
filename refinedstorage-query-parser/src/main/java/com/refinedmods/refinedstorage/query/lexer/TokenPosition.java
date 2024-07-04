package com.refinedmods.refinedstorage.query.lexer;

public record TokenPosition(Source source, TokenRange range) {
    @Override
    public String toString() {
        return "TokenPosition{"
            + "source=" + source
            + ", range=" + range
            + '}';
    }
}

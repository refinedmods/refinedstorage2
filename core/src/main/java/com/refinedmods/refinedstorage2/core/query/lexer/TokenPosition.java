package com.refinedmods.refinedstorage2.core.query.lexer;

public record TokenPosition(Source source, TokenRange range) {
    public TokenRange getRange() {
        return range;
    }

    public Source getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "TokenPosition{" +
                "source=" + source +
                ", range=" + range +
                '}';
    }
}

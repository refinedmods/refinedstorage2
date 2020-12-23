package com.refinedmods.refinedstorage2.core.query.lexer;

public class TokenPosition {
    private final Source source;
    private final TokenRange range;

    public TokenPosition(Source source, TokenRange range) {
        this.source = source;
        this.range = range;
    }

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

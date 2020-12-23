package com.refinedmods.refinedstorage2.core.query.parser;

public enum UnaryOperatorPosition {
    PREFIX,
    SUFFIX,
    BOTH;

    public boolean canUseAsPrefix() {
        return this == PREFIX || this == BOTH;
    }

    public boolean canUseAsSuffix() {
        return this == SUFFIX || this == BOTH;
    }
}

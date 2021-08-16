package com.refinedmods.refinedstorage2.core.query.parser;

public record Operator(int level, Associativity associativity) {
    public int getLevel() {
        return level;
    }

    public Associativity getAssociativity() {
        return associativity;
    }
}

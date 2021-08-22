package com.refinedmods.refinedstorage2.query.parser;

public record Operator(int level, Associativity associativity) {
    public int getLevel() {
        return level;
    }

    public Associativity getAssociativity() {
        return associativity;
    }
}

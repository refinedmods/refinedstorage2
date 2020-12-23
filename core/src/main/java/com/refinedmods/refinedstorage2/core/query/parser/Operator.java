package com.refinedmods.refinedstorage2.core.query.parser;

public class Operator {
    private final int level;
    private final Associativity associativity;

    public Operator(int level, Associativity associativity) {
        this.level = level;
        this.associativity = associativity;
    }

    public int getLevel() {
        return level;
    }

    public Associativity getAssociativity() {
        return associativity;
    }
}

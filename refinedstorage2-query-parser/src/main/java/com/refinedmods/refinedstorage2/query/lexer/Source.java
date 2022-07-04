package com.refinedmods.refinedstorage2.query.lexer;

public record Source(String name, String content) {
    @Override
    public String toString() {
        return "Source{"
            + "name='" + name + '\''
            + '}';
    }
}

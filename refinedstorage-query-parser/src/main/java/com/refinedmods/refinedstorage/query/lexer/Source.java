package com.refinedmods.refinedstorage.query.lexer;

public record Source(String name, String content) {
    @Override
    public String toString() {
        return "Source{"
            + "name='" + name + '\''
            + '}';
    }
}

package com.refinedmods.refinedstorage2.core.query.lexer;

public record Source(String name, String content) {
    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Source{" +
                "name='" + name + '\'' +
                '}';
    }
}

package com.refinedmods.refinedstorage2.core.query.lexer;

public class Source {
    private final String name;
    private final String content;

    public Source(String name, String content) {
        this.name = name;
        this.content = content;
    }

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

package com.refinedmods.refinedstorage2.api.core.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Filter<T> {
    private final Set<T> templates = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCK;

    public FilterMode getMode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(T template) {
        return switch (mode) {
            case ALLOW -> templates.contains(template);
            case BLOCK -> !templates.contains(template);
        };
    }

    public void setTemplates(List<T> templates) {
        this.templates.clear();
        this.templates.addAll(templates);
    }
}

package com.refinedmods.refinedstorage2.api.stack.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Move to core package
public class Filter<R> {
    private final Set<R> templates = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCK;

    public FilterMode getMode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(R template) {
        return switch (mode) {
            case ALLOW -> templates.contains(template);
            case BLOCK -> !templates.contains(template);
        };
    }

    public void setTemplates(List<R> templates) {
        this.templates.clear();
        this.templates.addAll(templates);
    }
}

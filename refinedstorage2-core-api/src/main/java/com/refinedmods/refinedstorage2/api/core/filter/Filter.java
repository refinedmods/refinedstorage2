package com.refinedmods.refinedstorage2.api.core.filter;

import java.util.HashSet;
import java.util.Set;

public class Filter {
    private final Set<Object> templates = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCK;

    public FilterMode getMode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(Object template) {
        return switch (mode) {
            case ALLOW -> templates.contains(template);
            case BLOCK -> !templates.contains(template);
        };
    }

    public void setTemplates(Set<Object> templates) {
        this.templates.clear();
        this.templates.addAll(templates);
    }
}

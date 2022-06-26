package com.refinedmods.refinedstorage2.api.core.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class Filter {
    private final Set<Object> templates = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCK;
    private UnaryOperator<Object> normalizer = value -> value;

    public FilterMode getMode() {
        return mode;
    }

    public void setNormalizer(final UnaryOperator<Object> normalizer) {
        this.normalizer = normalizer;
    }

    public void setMode(final FilterMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(final Object template) {
        Object normalized = normalizer.apply(template);
        return switch (mode) {
            case ALLOW -> templates.contains(normalized);
            case BLOCK -> !templates.contains(normalized);
        };
    }

    public void setTemplates(final Set<Object> templates) {
        this.templates.clear();
        this.templates.addAll(templates.stream().map(normalizer).collect(Collectors.toSet()));
    }
}

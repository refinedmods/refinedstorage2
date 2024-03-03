package com.refinedmods.refinedstorage2.api.resource.filter;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class Filter {
    private final Set<ResourceKey> templates = new HashSet<>();
    private FilterMode mode = FilterMode.BLOCK;
    private UnaryOperator<ResourceKey> normalizer = value -> value;

    public FilterMode getMode() {
        return mode;
    }

    public void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        this.normalizer = normalizer;
    }

    public void setMode(final FilterMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(final ResourceKey template) {
        final ResourceKey normalized = normalizer.apply(template);
        return switch (mode) {
            case ALLOW -> templates.contains(normalized);
            case BLOCK -> !templates.contains(normalized);
        };
    }

    public void setTemplates(final Set<ResourceKey> templates) {
        this.templates.clear();
        this.templates.addAll(templates.stream().map(normalizer).collect(Collectors.toSet()));
    }
}

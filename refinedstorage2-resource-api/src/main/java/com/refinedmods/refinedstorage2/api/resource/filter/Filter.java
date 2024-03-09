package com.refinedmods.refinedstorage2.api.resource.filter;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class Filter {
    private final Set<ResourceKey> filters = new HashSet<>();
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

    public boolean isAllowed(final ResourceKey resource) {
        final ResourceKey normalized = normalizer.apply(resource);
        return switch (mode) {
            case ALLOW -> filters.contains(normalized);
            case BLOCK -> !filters.contains(normalized);
        };
    }

    public void setFilters(final Set<ResourceKey> filters) {
        this.filters.clear();
        this.filters.addAll(filters.stream().map(normalizer).collect(Collectors.toSet()));
    }
}

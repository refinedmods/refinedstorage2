package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Represents a resource in the grid.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public abstract class AbstractGridResource {
    private final ResourceAmount<?> resourceAmount;
    private final String name;
    private final Map<GridResourceAttributeKey, Set<String>> attributes;
    private boolean zeroed;

    protected AbstractGridResource(final ResourceAmount<?> resourceAmount,
                                   final String name,
                                   final Map<GridResourceAttributeKey, Set<String>> attributes) {
        this.resourceAmount = resourceAmount;
        this.name = name;
        this.attributes = attributes;
    }

    public ResourceAmount<?> getResourceAmount() {
        return resourceAmount;
    }

    public abstract int getId();

    public String getName() {
        return name;
    }

    public Set<String> getAttribute(final GridResourceAttributeKey key) {
        return attributes.getOrDefault(key, Collections.emptySet());
    }

    public boolean isZeroed() {
        return zeroed;
    }

    public void setZeroed(final boolean zeroed) {
        this.zeroed = zeroed;
    }
}

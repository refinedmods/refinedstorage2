package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public abstract class AbstractPlatformGridResource<T> implements PlatformGridResource {
    protected final ResourceAmount<T> resourceAmount;
    private final String name;
    private final Map<GridResourceAttributeKey, Set<String>> attributes;
    private boolean zeroed;

    protected AbstractPlatformGridResource(final ResourceAmount<T> resourceAmount,
                                           final String name,
                                           final Map<GridResourceAttributeKey, Set<String>> attributes) {
        this.resourceAmount = resourceAmount;
        this.name = name;
        this.attributes = attributes;
    }

    public T getResource() {
        return resourceAmount.getResource();
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final GridView view) {
        return view.getTrackedResource(getResource());
    }

    @Override
    public long getAmount() {
        return resourceAmount.getAmount();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getAttribute(final GridResourceAttributeKey key) {
        return attributes.getOrDefault(key, Collections.emptySet());
    }

    @Override
    public boolean isZeroed() {
        return zeroed;
    }

    @Override
    public void setZeroed(final boolean zeroed) {
        this.zeroed = zeroed;
    }

    @Override
    public String toString() {
        return "AbstractPlatformGridResource{"
            + "resourceAmount=" + resourceAmount
            + ", name='" + name + '\''
            + ", attributes=" + attributes
            + ", zeroed=" + zeroed
            + '}';
    }
}

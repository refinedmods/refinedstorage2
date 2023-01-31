package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GridResourceImpl implements GridResource {
    private final ResourceAmount<?> resourceAmount;
    private final Map<GridResourceAttributeKey, Set<String>> attributes;
    private boolean zeroed;

    public GridResourceImpl(final String name, final long amount) {
        this(new ResourceAmount<>(name, amount));
    }

    public GridResourceImpl(final String name) {
        this(new ResourceAmount<>(name, 1));
    }

    public GridResourceImpl(final ResourceAmount<?> resourceAmount) {
        this.resourceAmount = resourceAmount;
        this.attributes = Map.of(
            FakeGridResourceAttributeKeys.MOD_ID, Set.of(resourceAmount.getResource().toString()),
            FakeGridResourceAttributeKeys.MOD_NAME, Set.of(resourceAmount.getResource().toString())
        );
    }

    public GridResourceImpl(final String name,
                            final long amount,
                            final String modId,
                            final String modName,
                            final Set<String> tags) {
        this.resourceAmount = new ResourceAmount<>(name, amount);
        this.attributes = Map.of(
            FakeGridResourceAttributeKeys.MOD_ID, Set.of(modId),
            FakeGridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            FakeGridResourceAttributeKeys.TAGS, tags
        );
    }

    public GridResourceImpl zeroed() {
        setZeroed(true);
        return this;
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final GridView view) {
        return view.getTrackedResource(resourceAmount.getResource());
    }

    @Override
    public long getAmount() {
        return resourceAmount.getAmount();
    }

    @Override
    public String getName() {
        return resourceAmount.getResource().toString();
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
        return resourceAmount.toString();
    }
}

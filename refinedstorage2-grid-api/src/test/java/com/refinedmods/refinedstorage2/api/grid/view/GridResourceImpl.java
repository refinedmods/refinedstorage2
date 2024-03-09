package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class GridResourceImpl implements GridResource {
    private final ResourceAmount resourceAmountRef;
    private final Map<GridResourceAttributeKey, Set<String>> attributes;
    private boolean zeroed;

    GridResourceImpl(final ResourceKey resource, final long amount) {
        this(new ResourceAmount(resource, amount));
    }

    GridResourceImpl(final ResourceAmount resourceAmountRef) {
        this.resourceAmountRef = resourceAmountRef;
        this.attributes = Map.of(
            FakeGridResourceAttributeKeys.MOD_ID, Set.of(resourceAmountRef.getResource().toString()),
            FakeGridResourceAttributeKeys.MOD_NAME, Set.of(resourceAmountRef.getResource().toString())
        );
    }

    public GridResourceImpl zeroed() {
        setZeroed(true);
        return this;
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final GridView view) {
        return view.getTrackedResource(resourceAmountRef.getResource());
    }

    @Override
    public long getAmount() {
        return resourceAmountRef.getAmount();
    }

    @Override
    public String getName() {
        return resourceAmountRef.getResource().toString();
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
        return resourceAmountRef.getResource().toString();
    }
}

package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class InterfaceExportStateImpl implements InterfaceExportState<String> {
    private final Map<Integer, ResourceAmount<String>> requested = new HashMap<>();
    private final Map<Integer, ResourceAmount<String>> current = new HashMap<>();

    private final int slots;

    public InterfaceExportStateImpl(final int slots) {
        this.slots = slots;
    }

    public void setRequestedResource(final int index, final String resource, final long amount) {
        requested.put(index, new ResourceAmount<>(resource, amount));
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Nullable
    @Override
    public String getRequestedResource(final int index) {
        final ResourceAmount<String> resourceAmount = this.requested.get(index);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResource();
    }

    @Override
    public long getRequestedResourceAmount(final int index) {
        final ResourceAmount<String> resourceAmount = this.requested.get(index);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Nullable
    @Override
    public String getCurrentlyExportedResource(final int index) {
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResource();
    }

    @Override
    public long getCurrentlyExportedResourceAmount(final int index) {
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Override
    public void setCurrentlyExported(final int index, final String resource, final long amount) {
        current.put(index, new ResourceAmount<>(resource, amount));
    }

    @Override
    public void decrementCurrentlyExportedAmount(final int index, final long amount) {
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount.getAmount() - amount <= 0) {
            this.current.remove(index);
        } else {
            resourceAmount.decrement(amount);
        }
    }

    @Override
    public void incrementCurrentlyExportedAmount(final int index, final long amount) {
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        resourceAmount.increment(amount);
    }
}

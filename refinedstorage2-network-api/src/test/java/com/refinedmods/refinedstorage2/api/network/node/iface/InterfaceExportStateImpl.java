package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public void clearRequestedResources() {
        requested.clear();
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    public Collection<String> expandExportCandidates(final StorageChannel<String> storageChannel,
                                                     final String resource) {
        if ("A".equals(resource)) {
            final List<String> candidates = new ArrayList<>();
            candidates.add("A");
            // simulate the behavior from FuzzyStorageChannel
            if (storageChannel.get("A1").isPresent()) {
                candidates.add("A1");
            }
            if (storageChannel.get("A2").isPresent()) {
                candidates.add("A2");
            }
            return candidates;
        }
        return Collections.singletonList(resource);
    }

    @Override
    public boolean isCurrentlyExportedResourceValid(final String want, final String got) {
        if ("A".equals(want)) {
            return got.startsWith("A");
        }
        return got.equals(want);
    }

    private void validateIndex(final int index) {
        if (index < 0 || index >= slots) {
            throw new IllegalArgumentException("Out of bounds: " + index);
        }
    }

    @Nullable
    @Override
    public String getRequestedResource(final int index) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.requested.get(index);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResource();
    }

    @Override
    public long getRequestedResourceAmount(final int index) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.requested.get(index);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Nullable
    @Override
    public String getCurrentlyExportedResource(final int index) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResource();
    }

    @Override
    public long getCurrentlyExportedResourceAmount(final int index) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Override
    public void setCurrentlyExported(final int index, final String resource, final long amount) {
        validateIndex(index);
        current.put(index, new ResourceAmount<>(resource, amount));
    }

    @Override
    public void decrementCurrentlyExportedAmount(final int index, final long amount) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        if (resourceAmount.getAmount() - amount <= 0) {
            this.current.remove(index);
        } else {
            resourceAmount.decrement(amount);
        }
    }

    @Override
    public void incrementCurrentlyExportedAmount(final int index, final long amount) {
        validateIndex(index);
        final ResourceAmount<String> resourceAmount = this.current.get(index);
        resourceAmount.increment(amount);
    }

    @Override
    public long insert(final String resource, final long amount, final Action action, final Actor actor) {
        for (int i = 0; i < getSlots(); ++i) {
            if (getCurrentlyExportedResource(i) == null) {
                if (action == Action.EXECUTE) {
                    setCurrentlyExported(i, resource, amount);
                }
                return amount;
            }
        }
        return 0;
    }
}

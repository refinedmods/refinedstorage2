package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class InterfaceExportStateImpl implements InterfaceExportState {
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
    @SuppressWarnings("unchecked")
    public <T> Collection<T> expandExportCandidates(final StorageChannel<T> storageChannel, final T resource) {
        if ("A".equals(resource)) {
            return (Collection<T>) expandExportCandidates((StorageChannel<String>) storageChannel);
        }
        return Collections.singletonList(resource);
    }

    private Collection<String> expandExportCandidates(final StorageChannel<String> storageChannel) {
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

    @Override
    public <A, B> boolean isExportedResourceValid(final ResourceTemplate<A> want, final ResourceTemplate<B> got) {
        if ("A".equals(want.resource())) {
            return ((String) got.resource()).startsWith("A");
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
    public ResourceTemplate<?> getRequestedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return new ResourceTemplate<>(resourceAmount.getResource(), NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }

    @Override
    public long getRequestedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Nullable
    @Override
    public ResourceTemplate<?> getExportedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return new ResourceTemplate<>(resourceAmount.getResource(), NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }

    @Override
    public long getExportedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Override
    public <T> void setExportSlot(final int slotIndex, final ResourceTemplate<T> resource, final long amount) {
        validateIndex(slotIndex);
        current.put(slotIndex, new ResourceAmount<>((String) resource.resource(), amount));
    }

    public void setCurrentlyExported(final int index, final String resource, final long amount) {
        setExportSlot(index, new ResourceTemplate<>(resource, NetworkTestFixtures.STORAGE_CHANNEL_TYPE), amount);
    }

    @Override
    public void shrinkExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = this.current.get(slotIndex);
        if (resourceAmount.getAmount() - amount <= 0) {
            this.current.remove(slotIndex);
        } else {
            resourceAmount.decrement(amount);
        }
    }

    @Override
    public void growExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        final ResourceAmount<String> resourceAmount = this.current.get(slotIndex);
        resourceAmount.increment(amount);
    }

    @Override
    public <T> long insert(final StorageChannelType<T> storageChannelType,
                           final T resource,
                           final long amount,
                           final Action action) {
        for (int i = 0; i < getSlots(); ++i) {
            if (getExportedResource(i) == null) {
                if (action == Action.EXECUTE) {
                    final ResourceTemplate<String> template = new ResourceTemplate<>(
                        (String) resource,
                        NetworkTestFixtures.STORAGE_CHANNEL_TYPE
                    );
                    setExportSlot(i, template, amount);
                }
                return amount;
            }
        }
        return 0;
    }

    @Override
    public <T> long extract(final T resource, final long amount, final Action action) {
        long extracted = 0;
        for (int i = 0; i < getSlots(); ++i) {
            final ResourceTemplate<?> slot = getExportedResource(i);
            if (slot != null && slot.resource().equals(resource)) {
                final long maxAmount = Math.min(getExportedAmount(i), amount - extracted);
                extracted += maxAmount;
                if (action == Action.EXECUTE) {
                    shrinkExportedAmount(i, maxAmount);
                }
                if (extracted == amount) {
                    break;
                }
            }
        }
        return extracted;
    }
}

package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
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

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A_ALTERNATIVE2;

public class InterfaceExportStateImpl implements InterfaceExportState {
    private final Map<Integer, ResourceAmount> requested = new HashMap<>();
    private final Map<Integer, ResourceAmount> current = new HashMap<>();
    private final int slots;

    public InterfaceExportStateImpl(final int slots) {
        this.slots = slots;
    }

    public void setRequestedResource(final int index, final ResourceKey resource, final long amount) {
        requested.put(index, new ResourceAmount(resource, amount));
    }

    public void clearRequestedResources() {
        requested.clear();
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    public Collection<ResourceKey> expandExportCandidates(final StorageChannel storageChannel,
                                                          final ResourceKey resource) {
        if (A.equals(resource)) {
            return expandExportCandidates(storageChannel);
        }
        return Collections.singletonList(resource);
    }

    private Collection<ResourceKey> expandExportCandidates(final StorageChannel storageChannel) {
        final List<ResourceKey> candidates = new ArrayList<>();
        candidates.add(A);
        // Simulate the behavior from FuzzyStorageChannel
        if (storageChannel.get(A_ALTERNATIVE).isPresent()) {
            candidates.add(A_ALTERNATIVE);
        }
        if (storageChannel.get(A_ALTERNATIVE2).isPresent()) {
            candidates.add(A_ALTERNATIVE2);
        }
        return candidates;
    }

    @Override
    public boolean isExportedResourceValid(final ResourceTemplate want, final ResourceTemplate got) {
        if (A.equals(want.resource())) {
            return got.resource() == A || got.resource() == A_ALTERNATIVE || got.resource() == A_ALTERNATIVE2;
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
    public ResourceTemplate getRequestedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return new ResourceTemplate(resourceAmount.getResource(), NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }

    @Override
    public long getRequestedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Nullable
    @Override
    public ResourceTemplate getExportedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return new ResourceTemplate(resourceAmount.getResource(), NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }

    @Override
    public long getExportedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.getAmount();
    }

    @Override
    public void setExportSlot(final int slotIndex, final ResourceTemplate resource, final long amount) {
        validateIndex(slotIndex);
        current.put(slotIndex, new ResourceAmount(resource.resource(), amount));
    }

    public void setCurrentlyExported(final int index, final ResourceKey resource, final long amount) {
        setExportSlot(index, new ResourceTemplate(resource, NetworkTestFixtures.STORAGE_CHANNEL_TYPE), amount);
    }

    @Override
    public void shrinkExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = this.current.get(slotIndex);
        if (resourceAmount.getAmount() - amount <= 0) {
            this.current.remove(slotIndex);
        } else {
            resourceAmount.decrement(amount);
        }
    }

    @Override
    public void growExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = this.current.get(slotIndex);
        resourceAmount.increment(amount);
    }

    @Override
    public long insert(final StorageChannelType storageChannelType,
                       final ResourceKey resource,
                       final long amount,
                       final Action action) {
        for (int i = 0; i < getSlots(); ++i) {
            if (getExportedResource(i) == null) {
                if (action == Action.EXECUTE) {
                    final ResourceTemplate template = new ResourceTemplate(
                        resource,
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
    public long extract(final ResourceKey resource, final long amount, final Action action) {
        long extracted = 0;
        for (int i = 0; i < getSlots(); ++i) {
            final ResourceTemplate slot = getExportedResource(i);
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

package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE2;
import static java.util.Objects.requireNonNull;

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
    public Collection<ResourceKey> expandExportCandidates(final RootStorage rootStorage,
                                                          final ResourceKey resource) {
        if (A.equals(resource)) {
            return expandExportCandidates(rootStorage);
        }
        return Collections.singletonList(resource);
    }

    private Collection<ResourceKey> expandExportCandidates(final RootStorage rootStorage) {
        final List<ResourceKey> candidates = new ArrayList<>();
        candidates.add(A);
        // Simulate the behavior from FuzzyRootStorage
        if (rootStorage.contains(A_ALTERNATIVE)) {
            candidates.add(A_ALTERNATIVE);
        }
        if (rootStorage.contains(A_ALTERNATIVE2)) {
            candidates.add(A_ALTERNATIVE2);
        }
        return candidates;
    }

    @Override
    public boolean isExportedResourceValid(final ResourceKey want, final ResourceKey got) {
        if (A.equals(want)) {
            return got == A || got == A_ALTERNATIVE || got == A_ALTERNATIVE2;
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
    public ResourceKey getRequestedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.resource();
    }

    @Override
    public long getRequestedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = requested.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.amount();
    }

    @Nullable
    @Override
    public ResourceKey getExportedResource(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.resource();
    }

    @Override
    public long getExportedAmount(final int slotIndex) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = current.get(slotIndex);
        if (resourceAmount == null) {
            return 0L;
        }
        return resourceAmount.amount();
    }

    @Override
    public void setExportSlot(final int slotIndex, final ResourceKey resource, final long amount) {
        validateIndex(slotIndex);
        current.put(slotIndex, new ResourceAmount(resource, amount));
    }

    public void setCurrentlyExported(final int index, final ResourceKey resource, final long amount) {
        setExportSlot(index, resource, amount);
    }

    @Override
    public void shrinkExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        final ResourceAmount resourceAmount = this.current.get(slotIndex);
        if (resourceAmount.amount() - amount <= 0) {
            this.current.remove(slotIndex);
        } else {
            this.current.put(
                slotIndex,
                new ResourceAmount(resourceAmount.resource(), resourceAmount.amount() - amount)
            );
        }
    }

    @Override
    public void growExportedAmount(final int slotIndex, final long amount) {
        validateIndex(slotIndex);
        this.current.compute(slotIndex, (k, resourceAmount) -> new ResourceAmount(
            requireNonNull(resourceAmount).resource(),
            resourceAmount.amount() + amount
        ));
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action) {
        for (int i = 0; i < getSlots(); ++i) {
            if (getExportedResource(i) == null) {
                if (action == Action.EXECUTE) {
                    setExportSlot(i, resource, amount);
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
            final ResourceKey slot = getExportedResource(i);
            if (slot != null && slot.equals(resource)) {
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

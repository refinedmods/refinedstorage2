package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class FakeSlottedImporterSource<T> implements SlottedImporterSource<T> {
    private final List<InMemoryStorageImpl<T>> slots = new ArrayList<>();

    public FakeSlottedImporterSource(final int slotAmount) {
        for (int i = 0; i < slotAmount; ++i) {
            slots.add(new InMemoryStorageImpl<>());
        }
    }

    public void setSlot(final int slot, final T resource, final long amount) {
        slots.get(slot).insert(resource, amount, Action.EXECUTE, EmptyActor.INSTANCE);
    }

    @Nullable
    public ResourceAmount<T> getResourceAmount(final int slot) {
        final Collection<ResourceAmount<T>> resources = slots.get(slot).getAll();
        if (resources.isEmpty()) {
            return null;
        }
        return new ArrayList<>(resources).get(0);
    }

    @Nullable
    @Override
    public T getResource(final int slot) {
        final ResourceAmount<T> resourceAmount = getResourceAmount(slot);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResource();
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public long extract(final int slot, final long amount, final Action action) {
        return slots.get(slot).extract(Objects.requireNonNull(getResource(slot)), amount, action, EmptyActor.INSTANCE);
    }
}

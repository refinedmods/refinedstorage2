package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.LazyCopyMutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Comparator;

class CraftingState {
    private final MutableResourceList storage;
    private final MutableResourceList internalStorage;
    private final Comparator<ResourceKey> sorter;

    private CraftingState(final MutableResourceList storage, final MutableResourceList internalStorage) {
        this.storage = storage;
        this.internalStorage = internalStorage;
        this.sorter = createSorter(storage).thenComparing(createSorter(internalStorage));
    }

    void extractFromInternalStorage(final ResourceKey resource, final long amount) {
        internalStorage.remove(resource, amount);
    }

    void extractFromStorage(final ResourceKey resource, final long amount) {
        storage.remove(resource, amount);
    }

    void addOutputsToInternalStorage(final Pattern pattern, final Amount amount) {
        pattern.layout().outputs().forEach(output -> addOutputToInternalStorage(amount, output));
    }

    private void addOutputToInternalStorage(final Amount amount, final ResourceAmount output) {
        final long totalAmount = output.amount() * amount.iterations();
        if (totalAmount < 0) {
            throw new NumberOverflowDuringCalculationException();
        }
        internalStorage.add(output.resource(), totalAmount);
    }

    CraftingState copy() {
        return new CraftingState(storage.copy(), internalStorage.copy());
    }

    static CraftingState of(final RootStorage rootStorage) {
        final MutableResourceListImpl storage = MutableResourceListImpl.create();
        rootStorage.getAll().forEach(storage::add);
        return new CraftingState(LazyCopyMutableResourceList.create(storage), MutableResourceListImpl.create());
    }

    Comparator<ResourceKey> getSorter() {
        return sorter;
    }

    private static Comparator<ResourceKey> createSorter(final MutableResourceList list) {
        return (a, b) -> {
            final long ar = list.get(a);
            final long br = list.get(b);
            return (int) br - (int) ar;
        };
    }

    ResourceState getResource(final ResourceKey resource) {
        return new ResourceState(resource, storage.get(resource), internalStorage.get(resource));
    }

    record ResourceState(ResourceKey resource, long inStorage, long inInternalStorage) {
        boolean isInStorage() {
            return inStorage > 0;
        }

        boolean isInInternalStorage() {
            return inInternalStorage > 0;
        }
    }
}

package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

public class NetworkNodeReferencingStorage<T> implements Storage<T> {
    private final NetworkNodeReference ref;
    private final Storage<T> fallback;

    public NetworkNodeReferencingStorage(NetworkNodeReference ref, Storage<T> fallback) {
        this.ref = ref;
        this.fallback = fallback;
    }

    private Storage<T> getStorage() {
        return ref.get()
            .filter(node -> node instanceof Storage)
            .map(node -> (Storage<T>) node)
            .orElse(fallback);
    }

    @Override
    public Optional<T> extract(T template, int amount, Action action) {
        return getStorage().extract(template, amount, action);
    }

    @Override
    public Optional<T> insert(T template, int amount, Action action) {
        return getStorage().insert(template, amount, action);
    }

    @Override
    public Collection<T> getStacks() {
        return getStorage().getStacks();
    }

    @Override
    public int getStored() {
        return getStorage().getStored();
    }
}

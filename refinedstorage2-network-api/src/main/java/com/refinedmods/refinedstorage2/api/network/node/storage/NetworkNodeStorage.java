package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Optional;

public class NetworkNodeStorage<T> extends ProxyStorage<T> implements TrackedStorage<T>, Priority {
    private final StorageNetworkNode<T> networkNode;

    public NetworkNodeStorage(StorageNetworkNode<T> networkNode, Storage<T> delegate) {
        super(delegate);
        this.networkNode = networkNode;
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        if (networkNode.getAccessMode() == AccessMode.INSERT || !networkNode.isActive()) {
            return 0;
        }
        return super.extract(resource, amount, action, source);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        if (networkNode.getAccessMode() == AccessMode.EXTRACT || !networkNode.isActive() || !networkNode.isAllowed(resource)) {
            return 0;
        }
        return super.insert(resource, amount, action, source);
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        return delegate instanceof TrackedStorage<T> trackedStorage
                ? trackedStorage.findTrackedResourceBySourceType(resource, sourceType)
                : Optional.empty();
    }

    @Override
    public int getPriority() {
        return networkNode.getPriority();
    }
}

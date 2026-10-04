package com.refinedmods.refinedstorage.api.network.node;

import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityProvider;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "3.3.0")
public interface StorageNetworkNodeDetailsProvider extends NetworkNodeDetailsProvider {
    @Nullable PriorityProvider getPriority();

    long getCapacity(Predicate<Storage> storageFilter);

    long getStored(Predicate<Storage> storageFilter);
}

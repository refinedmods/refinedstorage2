package com.refinedmods.refinedstorage.api.network.storage;

import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public interface StorageNetworkComponent extends NetworkComponent, StorageChannel {
    List<TrackedResourceAmount> getResources(Class<? extends Actor> actorType);

    boolean contains(Storage storage);
}

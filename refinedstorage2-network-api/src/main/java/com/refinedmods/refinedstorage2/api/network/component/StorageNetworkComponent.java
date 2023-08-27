package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.1")
public interface StorageNetworkComponent extends NetworkComponent {
    <T> StorageChannel<T> getStorageChannel(StorageChannelType<T> type);

    boolean hasSource(Predicate<Storage<?>> matcher);

    <T> List<TrackedResourceAmount<T>> getResources(StorageChannelType<T> type, Class<? extends Actor> actorType);
}

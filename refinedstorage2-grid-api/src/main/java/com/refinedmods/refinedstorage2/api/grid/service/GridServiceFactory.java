package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.function.ToLongFunction;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridServiceFactory {
    <T> GridService<T> create(
        StorageChannelType<T> storageChannelType,
        Actor actor,
        ToLongFunction<T> maxAmountProvider,
        long singleAmount
    );
}

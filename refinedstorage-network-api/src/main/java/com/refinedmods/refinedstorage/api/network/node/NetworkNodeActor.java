package com.refinedmods.refinedstorage.api.network.node;

import com.refinedmods.refinedstorage.api.storage.Actor;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public record NetworkNodeActor(NetworkNode networkNode) implements Actor {
    @Override
    public String getName() {
        return networkNode.getClass().getName();
    }
}

package com.refinedmods.refinedstorage.common.api.support.network.item;

import com.refinedmods.refinedstorage.api.network.Network;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface NetworkItemContext {
    Optional<Network> resolveNetwork(boolean force);

    default Optional<Network> resolveNetwork() {
        return resolveNetwork(false);
    }

    boolean isActive();

    void drainEnergy(long amount);
}

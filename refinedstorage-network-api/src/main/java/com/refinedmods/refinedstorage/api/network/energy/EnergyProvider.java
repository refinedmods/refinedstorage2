package com.refinedmods.refinedstorage.api.network.energy;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public interface EnergyProvider {
    long getStored();

    long getCapacity();

    long extract(long amount);

    default boolean contains(EnergyProvider energyProvider) {
        return false;
    }
}

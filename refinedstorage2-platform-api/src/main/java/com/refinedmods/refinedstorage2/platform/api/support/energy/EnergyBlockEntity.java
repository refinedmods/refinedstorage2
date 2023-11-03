package com.refinedmods.refinedstorage2.platform.api.support.energy;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import org.apiguardian.api.API;

/**
 * Implement this on block entities that can contain energy.
 * At runtime, Refined Storage will attach an energy storage adapter for the platform.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface EnergyBlockEntity {
    EnergyStorage getEnergyStorage();
}

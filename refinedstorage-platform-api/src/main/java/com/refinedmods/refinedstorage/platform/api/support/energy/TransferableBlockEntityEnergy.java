package com.refinedmods.refinedstorage.platform.api.support.energy;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface TransferableBlockEntityEnergy {
    EnergyStorage getEnergyStorage();
}

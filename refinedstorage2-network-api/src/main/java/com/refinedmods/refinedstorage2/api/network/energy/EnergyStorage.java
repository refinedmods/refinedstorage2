package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface EnergyStorage {
    /**
     * @return the amount stored
     */
    long getStored();

    /**
     * @return the capacity
     */
    long getCapacity();

    /**
     * Adds energy to the energy storage.
     *
     * @param amount the amount of energy to be received
     * @param action the action
     * @return the amount received
     */
    long receive(long amount, Action action);

    /**
     * Extracts energy from the energy storage.
     *
     * @param amount the amount of energy to be extracted
     * @param action the action
     * @return the amount extracted
     */
    long extract(long amount, Action action);
}

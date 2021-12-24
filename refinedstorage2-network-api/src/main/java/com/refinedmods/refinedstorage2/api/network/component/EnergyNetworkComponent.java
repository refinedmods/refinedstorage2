package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

public class EnergyNetworkComponent implements NetworkComponent {
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

    @Override
    public void onContainerAdded(NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyStorage source) {
            energyStorage.addSource(source);
        }
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyStorage source) {
            energyStorage.removeSource(source);
        }
    }

    public long getStored() {
        return energyStorage.getStored();
    }

    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    public long extract(long amount) {
        return energyStorage.extract(amount, Action.EXECUTE);
    }
}

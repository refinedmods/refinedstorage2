package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;

public class EnergyNetworkComponent implements NetworkComponent {
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

    @Override
    public void onContainerAdded(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof EnergyStorage) {
            energyStorage.addSource((EnergyStorage) container.getNode());
        }
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof EnergyStorage) {
            energyStorage.removeSource((EnergyStorage) container.getNode());
        }
    }

    public CompositeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}

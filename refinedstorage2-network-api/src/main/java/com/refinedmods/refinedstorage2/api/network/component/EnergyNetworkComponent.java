package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

public class EnergyNetworkComponent implements NetworkComponent {
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

    @Override
    public void onContainerAdded(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof EnergyStorage nodeEnergyStorage) {
            energyStorage.addSource(nodeEnergyStorage);
        }
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer<?> container) {
        if (container.getNode() instanceof EnergyStorage nodeEnergyStorage) {
            energyStorage.removeSource(nodeEnergyStorage);
        }
    }

    public CompositeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}

package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

public class EnergyNetworkComponent implements NetworkComponent {
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

    @Override
    public void onHostAdded(NetworkNodeHost<?> host) {
        if (host.getNode() instanceof EnergyStorage) {
            energyStorage.addSource((EnergyStorage) host.getNode());
        }
    }

    @Override
    public void onHostRemoved(NetworkNodeHost<?> host) {
        if (host.getNode() instanceof EnergyStorage) {
            energyStorage.removeSource((EnergyStorage) host.getNode());
        }
    }

    public CompositeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}

package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

import java.util.Set;

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

    @Override
    public void onNetworkRemoved() {

    }

    @Override
    public void onNetworkSplit(Set<Network> networks) {

    }

    public CompositeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}

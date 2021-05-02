package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.EnergyStorage;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Optional;

public class NetworkNodeReferencingEnergyStorage implements EnergyStorage {
    private final NetworkNodeReference ref;

    public NetworkNodeReferencingEnergyStorage(NetworkNodeReference ref) {
        this.ref = ref;
    }

    @Override
    public long getStored() {
        return getStorage().map(EnergyStorage::getStored).orElse(0L);
    }

    @Override
    public long getCapacity() {
        return getStorage().map(EnergyStorage::getCapacity).orElse(0L);
    }

    @Override
    public void setCapacity(long capacity) {
        getStorage().ifPresent(energyStorage -> energyStorage.setCapacity(capacity));
    }

    @Override
    public long receive(long amount, Action action) {
        return getStorage().map(energyStorage -> energyStorage.receive(amount, action)).orElse(amount);
    }

    @Override
    public long extract(long amount, Action action) {
        return getStorage().map(energyStorage -> energyStorage.extract(amount, action)).orElse(0L);
    }

    private Optional<EnergyStorage> getStorage() {
        return ref.get()
                .filter(node -> node instanceof EnergyStorage)
                .map(node -> (EnergyStorage) node);
    }
}

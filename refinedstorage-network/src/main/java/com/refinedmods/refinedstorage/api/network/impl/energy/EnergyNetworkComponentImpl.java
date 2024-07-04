package com.refinedmods.refinedstorage.api.network.impl.energy;

import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import java.util.LinkedHashSet;
import java.util.Set;

public class EnergyNetworkComponentImpl implements EnergyNetworkComponent {
    private final Set<EnergyProvider> providers = new LinkedHashSet<>();

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyProvider provider) {
            providers.add(provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof EnergyProvider provider) {
            providers.remove(provider);
        }
    }

    @Override
    public long getStored() {
        long stored = 0;
        for (final EnergyProvider provider : providers) {
            if (stored + provider.getStored() < 0) {
                return Long.MAX_VALUE;
            }
            stored += provider.getStored();
        }
        return stored;
    }

    @Override
    public long getCapacity() {
        long capacity = 0;
        for (final EnergyProvider provider : providers) {
            if (capacity + provider.getCapacity() < 0) {
                return Long.MAX_VALUE;
            }
            capacity += provider.getCapacity();
        }
        return capacity;
    }

    @Override
    public long extract(final long amount) {
        long extracted = 0;
        for (final EnergyProvider provider : providers) {
            extracted += provider.extract(amount - extracted);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }

    @Override
    public boolean contains(final EnergyProvider energyProvider) {
        for (final EnergyProvider provider : providers) {
            if (provider.contains(energyProvider)) {
                return true;
            }
        }
        return false;
    }
}

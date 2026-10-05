package com.refinedmods.refinedstorage.api.network.impl.node.controller;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.MergedBaseNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeEventManager;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.MergedNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;

import org.jspecify.annotations.Nullable;

public class ControllerNetworkNode extends AbstractNetworkNode implements EnergyProvider, NetworkNodeDetailsProvider {
    private static final int ENERGY_CHANGE_NOTIFICATION_INTERVAL = 5;

    private final NetworkNodeType type;
    private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();

    @Nullable
    private EnergyStorage energyStorage;
    private long notifiedStored;
    private long notifiedCapacity;
    private int ticks;

    public ControllerNetworkNode(final NetworkNodeType type) {
        this.type = type;
    }

    public void setEnergyStorage(@Nullable final EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public ControllerEnergyState getState() {
        if (!isActive() || energyStorage == null) {
            return ControllerEnergyState.OFF;
        }
        final double pct = (double) energyStorage.getStored() / (double) energyStorage.getCapacity();
        return getState(pct);
    }

    private ControllerEnergyState getState(final double pct) {
        if (pct >= 0.4) {
            return ControllerEnergyState.ON;
        }
        if (pct >= 0.3) {
            return ControllerEnergyState.NEARLY_ON;
        }
        if (pct >= 0.01) {
            return ControllerEnergyState.NEARLY_OFF;
        }
        return ControllerEnergyState.OFF;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (++ticks % ENERGY_CHANGE_NOTIFICATION_INTERVAL == 0) {
            notifyEnergyChanged();
        }
    }

    private void notifyEnergyChanged() {
        if (!eventManager.hasListeners()) {
            return;
        }
        final long stored = getActualStored();
        final long capacity = getActualCapacity();
        if (stored == notifiedStored && capacity == notifiedCapacity) {
            return;
        }
        notifiedStored = stored;
        notifiedCapacity = capacity;
        eventManager.notifyListeners(new ControllerEnergyChangedEvent(stored, capacity));
    }

    @Override
    public long getStored() {
        if (!isActive()) {
            return 0;
        }
        return getActualStored();
    }

    public long getActualStored() {
        return energyStorage == null ? 0L : energyStorage.getStored();
    }

    @Override
    public long getCapacity() {
        if (!isActive()) {
            return 0;
        }
        return getActualCapacity();
    }

    public long getActualCapacity() {
        return energyStorage == null ? 0L : energyStorage.getCapacity();
    }

    @Override
    public long extract(final long amount) {
        if (energyStorage == null) {
            return 0L;
        }
        return energyStorage.extract(amount, Action.EXECUTE);
    }

    @Override
    public long getEnergyUsage() {
        return 0L;
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        eventManager.notifyDetailsChanged(getEnergyUsage(), newActive);
    }

    @Override
    public NetworkNodeType getType() {
        return type;
    }

    @Override
    public NetworkNodeDetails createDetails() {
        return SimpleNetworkNodeDetails.of(this);
    }

    @Override
    public void mergeDetails(final MergedNetworkNodeDetails details) {
        details.getOrCreate(MergedBaseNetworkNodeDetails.ELEMENT).merge(getEnergyUsage(), isActive());
    }

    @Override
    public void addListener(final NetworkNodeListener listener) {
        eventManager.addListener(listener);
    }

    @Override
    public void removeListener(final NetworkNodeListener listener) {
        eventManager.removeListener(listener);
    }
}

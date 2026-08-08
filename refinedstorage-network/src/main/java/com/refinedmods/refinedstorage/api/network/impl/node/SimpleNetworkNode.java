package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;

public class SimpleNetworkNode extends AbstractNetworkNode implements NetworkNodeDetailsProvider {
    private final NetworkNodeType type;
    private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();
    private long energyUsage;

    public SimpleNetworkNode(final NetworkNodeType type, final long energyUsage) {
        this.type = type;
        this.energyUsage = energyUsage;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
        eventManager.notifyDetailsChanged(energyUsage, isActive());
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
    public long getEnergyUsage() {
        return energyUsage;
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

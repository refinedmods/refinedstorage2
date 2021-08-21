package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.component.EnergyNetworkComponent;

public abstract class NetworkNodeImpl implements NetworkNode {
    protected Rs2World world;
    protected final Position position;
    protected Network network;
    protected RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    private boolean wasActive;

    protected NetworkNodeImpl(Position position) {
        this.position = position;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void setWorld(Rs2World world) {
        this.world = world;
    }

    public boolean isActive() {
        return redstoneMode.isActive(world.isPowered(position))
                && network.getComponent(EnergyNetworkComponent.class).getEnergyStorage().getStored() > 0;
    }

    protected void onActiveChanged(boolean active) {
    }

    @Override
    public void update() {
        network.getComponent(EnergyNetworkComponent.class).getEnergyStorage().extract(getEnergyUsage(), Action.EXECUTE);

        if (wasActive != isActive()) {
            wasActive = isActive();
            onActiveChanged(wasActive);
        }
    }

    protected abstract long getEnergyUsage();

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }
}

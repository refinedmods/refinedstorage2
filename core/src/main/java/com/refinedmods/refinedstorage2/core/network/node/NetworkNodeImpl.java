package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

public abstract class NetworkNodeImpl implements NetworkNode {
    protected final Rs2World world;
    private final Position pos;
    private final NetworkNodeReference ref;
    protected Network network;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected NetworkNodeImpl(Rs2World world, Position pos, NetworkNodeReference ref) {
        this.world = world;
        this.pos = pos;
        this.ref = ref;
    }

    @Override
    public void update() {
        network.getEnergyStorage().extract(getEnergyUsage(), Action.EXECUTE);
    }

    @Override
    public Position getPosition() {
        return pos;
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
    public NetworkNodeReference createReference() {
        return ref;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
    }

    @Override
    public boolean isActive() {
        return redstoneMode.isActive(world.isPowered(pos)) && network.getEnergyStorage().getStored() > 0;
    }

    @Override
    public void onActiveChanged(boolean active) {
    }

    public abstract long getEnergyUsage();
}

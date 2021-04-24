package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.util.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkNodeImpl implements NetworkNode {
    private static final Logger LOGGER = LogManager.getLogger(NetworkNodeImpl.class);

    protected final Rs2World world;
    private final Position pos;
    private final NetworkNodeReference ref;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    protected Network network;

    public NetworkNodeImpl(Rs2World world, Position pos, NetworkNodeReference ref) {
        this.world = world;
        this.pos = pos;
        this.ref = ref;
    }

    @Override
    public Position getPosition() {
        return pos;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public Network getNetwork() {
        return network;
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
        return redstoneMode.isActive(world.isPowered(pos));
    }

    @Override
    public void onActiveChanged(boolean active) {
        LOGGER.info("Active changed to {} for node at {}", active, pos);
    }
}

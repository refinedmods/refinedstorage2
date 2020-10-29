package com.refinedmods.refinedstorage2.fabric.coreimpl.network;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.Collection;

public class FabricNetworkManager extends PersistentState implements NetworkManager {
    public static final String NAME = "refinedstorage2_networks";

    private final NetworkManager parent;

    public FabricNetworkManager(String name, NetworkManager parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public Network onNodeAdded(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        return parent.onNodeAdded(nodeAdapter, pos);
    }

    @Override
    public void onNodeRemoved(NetworkNodeAdapter nodeAdapter, BlockPos pos) {
        parent.onNodeRemoved(nodeAdapter, pos);
    }

    @Override
    public Collection<Network> getNetworks() {
        return parent.getNetworks();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        // TODO
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        // TODO
        return tag;
    }
}

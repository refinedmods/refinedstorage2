package com.refinedmods.refinedstorage2.fabric.coreimpl.network;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
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
    public Network onNodeAdded(NetworkNodeRepository repository, BlockPos pos) {
        Network network = parent.onNodeAdded(repository, pos);
        markDirty();
        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNodeRepository repository, BlockPos pos) {
        parent.onNodeRemoved(repository, pos);
        markDirty();
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

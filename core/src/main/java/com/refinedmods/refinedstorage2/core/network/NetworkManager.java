package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NetworkManager {
    Network onNodeAdded(BlockPos pos);

    void onNodeRemoved(NetworkNode node);

    Optional<Network> getNetwork(UUID id);

    Collection<Network> getNetworks();
}

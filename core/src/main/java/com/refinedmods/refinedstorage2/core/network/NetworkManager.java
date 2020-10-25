package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.adapter.WorldAdapter;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

public interface NetworkManager {
    Network onNodeAdded(WorldAdapter worldAdapter, BlockPos pos);

    void onNodeRemoved(BlockPos pos);

    Optional<Network> getNetwork(UUID id);
}

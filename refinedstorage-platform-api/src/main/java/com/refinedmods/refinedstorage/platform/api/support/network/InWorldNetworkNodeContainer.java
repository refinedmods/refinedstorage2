package com.refinedmods.refinedstorage.platform.api.support.network;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface InWorldNetworkNodeContainer extends NetworkNodeContainer, ConnectionLogic {
    BlockState getBlockState();

    boolean isRemoved();

    GlobalPos getPosition();

    BlockPos getLocalPosition();

    String getName();
}

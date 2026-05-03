package com.refinedmods.refinedstorage.common.api.support.network;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface InWorldNetworkNodeContainer extends NetworkNodeContainer, ConnectionStrategy {
    BlockState getBlockState();

    boolean isRemoved();

    GlobalPos getPosition();

    BlockPos getLocalPosition();

    String getName();

    BlockEntity getBlockEntity();

    interface Builder {
        Builder name(String name);

        Builder priority(int priority);

        Builder connectionStrategy(ConnectionStrategy connectionStrategy);

        Builder keyProvider(Supplier<Object> keyProvider);

        InWorldNetworkNodeContainer build();
    }
}

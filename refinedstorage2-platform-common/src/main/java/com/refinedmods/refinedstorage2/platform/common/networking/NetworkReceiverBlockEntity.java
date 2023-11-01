package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeContainerBlockEntityImpl;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkReceiverBlockEntity extends NetworkNodeContainerBlockEntityImpl<SimpleNetworkNode> {
    public NetworkReceiverBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getNetworkReceiver(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getNetworkReceiver().getEnergyUsage())
        );
    }

    @Nullable
    @Override
    public Object createKey() {
        return new NetworkReceiverKey(getContainerPosition());
    }
}

package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.ColoredConnectionStrategy;
import com.refinedmods.refinedstorage.common.support.network.PlatformNetworkNodeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public class NetworkReceiverBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    public NetworkReceiverBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getNetworkReceiver(), pos, state, new SimpleNetworkNode(
            PlatformNetworkNodeTypes.NETWORK_RECEIVER,
            Platform.INSTANCE.getConfig().getNetworkReceiver().getEnergyUsage()
        ));
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final SimpleNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new ColoredConnectionStrategy(this::getBlockState, getBlockPos()))
            .keyProvider(this::createKey)
            .build();
    }

    @Override
    protected boolean hasRedstoneMode() {
        return false;
    }

    private NetworkReceiverKey createKey() {
        return new NetworkReceiverKey(GlobalPos.of(requireNonNull(level).dimension(), getBlockPos()));
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.NETWORK_RECEIVER);
    }
}

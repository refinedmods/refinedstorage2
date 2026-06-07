package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkMonitorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<NetworkMonitorData> {
    public NetworkMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getNetworkMonitor(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getNetworkMonitor().getEnergyUsage())
        );
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.NETWORK_MONITOR);
    }

    @Override
    public NetworkMonitorData getMenuData() {
        return NetworkMonitorData.INSTANCE;
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, NetworkMonitorData> getMenuCodec() {
        return NetworkMonitorData.STREAM_CODEC;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new NetworkMonitorContainerMenu(syncId, this);
    }
}

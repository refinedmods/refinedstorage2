package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorListener;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class NetworkMonitorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<MonitorNetworkNode>
    implements NetworkNodeExtendedMenuProvider<NetworkMonitorData> {
    public NetworkMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getNetworkMonitor(),
            pos,
            state,
            new MonitorNetworkNode(Platform.INSTANCE.getConfig().getNetworkMonitor().getEnergyUsage())
        );
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.NETWORK_MONITOR);
    }

    @Override
    public NetworkMonitorData getMenuData() {
        return new NetworkMonitorData(mainNetworkNode.getTypes().stream().map(this::toDeviceGroup).toList());
    }

    private NetworkMonitorDeviceGroup toDeviceGroup(final NetworkNodeType nodeType) {
        final NetworkMonitorDeviceType deviceType = RefinedStorageApi.INSTANCE.getNetworkMonitorDeviceType(nodeType);
        final List<NetworkMonitorDevice> devices = toDevices(nodeType);
        final MonitorNodeTypeId typeId = requireNonNull(mainNetworkNode.getTypeId(nodeType));
        return new NetworkMonitorDeviceGroup(typeId.id(), deviceType, devices);
    }

    private List<NetworkMonitorDevice> toDevices(final NetworkNodeType nodeType) {
        return mainNetworkNode.getNodes(nodeType).stream().map(this::toDevice).filter(Objects::nonNull).toList();
    }

    @Nullable
    public NetworkMonitorDevice getDevice(final MonitorNodeId id) {
        final NetworkNode node = mainNetworkNode.getNode(id);
        if (node == null) {
            return null;
        }
        return toDevice(node);
    }

    @Nullable
    public NetworkMonitorDeviceType getDeviceType(final MonitorNodeTypeId typeId) {
        final NetworkNodeType nodeType = mainNetworkNode.getType(typeId);
        if (nodeType == null) {
            return null;
        }
        return RefinedStorageApi.INSTANCE.getNetworkMonitorDeviceType(nodeType);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private NetworkMonitorDevice toDevice(final NetworkNode node) {
        final MonitorNodeId id = requireNonNull(mainNetworkNode.getId(node));
        if (!(mainNetworkNode.getContainer(id) instanceof InWorldNetworkNodeContainer inWorldNetworkNodeContainer)) {
            return null;
        }
        final Block block = inWorldNetworkNodeContainer.getBlockState().getBlock();
        return new NetworkMonitorDevice(id.id(), block.getName(), block.asItem().builtInRegistryHolder());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, NetworkMonitorData> getMenuCodec() {
        return NetworkMonitorData.STREAM_CODEC;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new NetworkMonitorContainerMenu(syncId, inventory, this);
    }

    public void addListener(final MonitorListener monitorListener) {
        mainNetworkNode.addListener(monitorListener);
    }

    public void removeListener(final MonitorListener monitorListener) {
        mainNetworkNode.removeListener(monitorListener);
    }
}

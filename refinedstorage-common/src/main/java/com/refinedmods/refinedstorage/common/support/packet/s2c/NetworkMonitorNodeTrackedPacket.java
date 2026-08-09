package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorDevice;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorNodeTrackedPacket(UUID groupId, NetworkMonitorDeviceType groupType,
                                              NetworkMonitorDevice device)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorNodeTrackedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_node_tracked")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorNodeTrackedPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorNodeTrackedPacket::groupId,
            NetworkMonitorDeviceType.STREAM_CODEC, NetworkMonitorNodeTrackedPacket::groupType,
            NetworkMonitorDevice.STREAM_CODEC, NetworkMonitorNodeTrackedPacket::device,
            NetworkMonitorNodeTrackedPacket::new
        );

    public static void handle(final NetworkMonitorNodeTrackedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.addDevice(new MonitorNodeTypeId(packet.groupId), packet.groupType(), packet.device());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}


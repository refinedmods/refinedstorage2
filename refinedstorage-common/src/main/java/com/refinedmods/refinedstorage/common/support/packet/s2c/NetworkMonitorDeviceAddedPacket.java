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

public record NetworkMonitorDeviceAddedPacket(UUID groupId, NetworkMonitorDeviceType groupType,
                                              NetworkMonitorDevice device)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorDeviceAddedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_device_added")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDeviceAddedPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorDeviceAddedPacket::groupId,
            NetworkMonitorDeviceType.STREAM_CODEC, NetworkMonitorDeviceAddedPacket::groupType,
            NetworkMonitorDevice.STREAM_CODEC, NetworkMonitorDeviceAddedPacket::device,
            NetworkMonitorDeviceAddedPacket::new
        );

    public static void handle(final NetworkMonitorDeviceAddedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.addDevice(new MonitorNodeTypeId(packet.groupId), packet.groupType(), packet.device());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}


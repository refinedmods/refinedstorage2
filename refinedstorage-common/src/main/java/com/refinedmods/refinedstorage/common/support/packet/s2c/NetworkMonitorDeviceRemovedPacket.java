package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorDeviceRemovedPacket(UUID deviceId)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorDeviceRemovedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_device_removed")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDeviceRemovedPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorDeviceRemovedPacket::deviceId,
            NetworkMonitorDeviceRemovedPacket::new
        );

    public static void handle(final NetworkMonitorDeviceRemovedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.removeDevice(new MonitorNodeId(packet.deviceId));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}


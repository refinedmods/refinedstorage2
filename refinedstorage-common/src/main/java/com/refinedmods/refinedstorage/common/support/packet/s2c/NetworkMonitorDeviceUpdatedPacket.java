package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorDeviceUpdatedPacket(UUID deviceId, long energyUsage, boolean active)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorDeviceUpdatedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_device_updated")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDeviceUpdatedPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorDeviceUpdatedPacket::deviceId,
            ByteBufCodecs.LONG, NetworkMonitorDeviceUpdatedPacket::energyUsage,
            ByteBufCodecs.BOOL, NetworkMonitorDeviceUpdatedPacket::active,
            NetworkMonitorDeviceUpdatedPacket::new
        );

    public static void handle(final NetworkMonitorDeviceUpdatedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.updateDevice(new MonitorNodeId(packet.deviceId), packet.energyUsage, packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

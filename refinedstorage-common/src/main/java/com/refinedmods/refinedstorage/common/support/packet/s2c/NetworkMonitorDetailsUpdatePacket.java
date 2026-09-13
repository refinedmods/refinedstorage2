package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorDetailsUpdatePacket(Optional<UUID> deviceId, NetworkNodeDetails details)
    implements CustomPacketPayload {
    public static final Type<NetworkMonitorDetailsUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_details_update")
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<UUID>> DEVICE_ID_STREAM_CODEC =
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC);

    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDetailsUpdatePacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                DEVICE_ID_STREAM_CODEC.encode(buf, packet.deviceId);
                final StreamCodec<RegistryFriendlyByteBuf, NetworkNodeDetails> codec =
                    (StreamCodec<RegistryFriendlyByteBuf, NetworkNodeDetails>) RefinedStorageApi.INSTANCE
                        .getNetworkNodeDetailsFactory(packet.details.getClass());
                final Identifier factoryId = RefinedStorageApi.INSTANCE.getNetworkNodeDetailsFactories()
                    .getId(codec)
                    .orElseThrow();
                buf.writeIdentifier(factoryId);
                codec.encode(buf, packet.details);
            },
            buf -> {
                final Optional<UUID> deviceId = DEVICE_ID_STREAM_CODEC.decode(buf);
                final Identifier factoryId = buf.readIdentifier();
                final var codec = RefinedStorageApi.INSTANCE.getNetworkNodeDetailsFactories()
                    .get(factoryId)
                    .orElseThrow();
                return new NetworkMonitorDetailsUpdatePacket(deviceId, codec.decode(buf));
            }
        );

    public static void handle(final NetworkMonitorDetailsUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.updateDetails(packet.deviceId.map(MonitorNodeId::new).orElse(null), packet.details);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

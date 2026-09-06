package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.networking.NetworkMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkMonitorDetailsUpdatePacket(NetworkNodeDetails details) implements CustomPacketPayload {
    public static final Type<NetworkMonitorDetailsUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_monitor_details_update")
    );

    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDetailsUpdatePacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
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
                final Identifier factoryId = buf.readIdentifier();
                final var codec = RefinedStorageApi.INSTANCE.getNetworkNodeDetailsFactories()
                    .get(factoryId)
                    .orElseThrow();
                return new NetworkMonitorDetailsUpdatePacket(codec.decode(buf));
            }
        );

    public static void handle(final NetworkMonitorDetailsUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof NetworkMonitorContainerMenu networkMonitor) {
            networkMonitor.updateDetails(packet.details);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

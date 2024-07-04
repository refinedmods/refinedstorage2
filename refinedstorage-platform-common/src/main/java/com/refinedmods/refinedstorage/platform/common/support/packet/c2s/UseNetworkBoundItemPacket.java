package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.energy.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record UseNetworkBoundItemPacket(SlotReference slotReference) implements CustomPacketPayload {
    public static final Type<UseNetworkBoundItemPacket> PACKET_TYPE = new Type<>(
        createIdentifier("use_network_bound_item")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UseNetworkBoundItemPacket> STREAM_CODEC = StreamCodec
        .composite(
            SlotReferenceFactory.STREAM_CODEC, UseNetworkBoundItemPacket::slotReference,
            UseNetworkBoundItemPacket::new
        );

    public static void handle(final UseNetworkBoundItemPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        packet.slotReference.resolve(player).ifPresent(stack -> {
            if (!(stack.getItem() instanceof AbstractNetworkBoundEnergyItem networkBoundItem)) {
                return;
            }
            final NetworkBoundItemSession sess = PlatformApi.INSTANCE.getNetworkBoundItemHelper().openSession(
                stack,
                (ServerPlayer) player,
                packet.slotReference
            );
            networkBoundItem.use((ServerPlayer) player, packet.slotReference, sess);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

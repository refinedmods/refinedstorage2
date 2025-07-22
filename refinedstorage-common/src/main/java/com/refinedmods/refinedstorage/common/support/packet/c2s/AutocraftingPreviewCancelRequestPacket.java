package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.api.autocrafting.CancelablePreviewProvider;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingPreviewCancelRequestPacket() implements CustomPacketPayload {
    public static final Type<AutocraftingPreviewCancelRequestPacket> PACKET_TYPE =
        new Type<>(createIdentifier("autocrafting_preview_cancel_request"));
    public static final AutocraftingPreviewCancelRequestPacket INSTANCE = new AutocraftingPreviewCancelRequestPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewCancelRequestPacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    public static void handle(final PacketContext ctx) {
        final AbstractContainerMenu containerMenu = ctx.getPlayer().containerMenu;
        if (containerMenu instanceof CancelablePreviewProvider provider) {
            final ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            provider.cancel();
            S2CPackets.sendAutocraftingPreviewCancelResponse(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

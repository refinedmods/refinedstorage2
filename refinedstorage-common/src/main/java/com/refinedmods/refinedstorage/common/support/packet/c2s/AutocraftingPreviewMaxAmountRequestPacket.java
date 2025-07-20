package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingPreviewMaxAmountRequestPacket(PlatformResourceKey resource) implements CustomPacketPayload {
    public static final Type<AutocraftingPreviewMaxAmountRequestPacket> PACKET_TYPE = new Type<>(createIdentifier(
        "autocrafting_preview_max_amount_request"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewMaxAmountRequestPacket> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.STREAM_CODEC, AutocraftingPreviewMaxAmountRequestPacket::resource,
            AutocraftingPreviewMaxAmountRequestPacket::new
        );

    public static void handle(final AutocraftingPreviewMaxAmountRequestPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PreviewProvider provider) {
            final ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            provider.getMaxAmount(packet.resource, new CancellationToken()).thenAccept(maxAmount ->
                S2CPackets.sendAutocraftingPreviewMaxAmountResponse(player, maxAmount));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

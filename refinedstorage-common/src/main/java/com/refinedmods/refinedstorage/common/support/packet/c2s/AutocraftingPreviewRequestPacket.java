package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingPreviewRequestPacket(UUID id,
                                               PlatformResourceKey resource,
                                               long amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AutocraftingPreviewRequestPacket>
        PACKET_TYPE = new CustomPacketPayload.Type<>(
        createIdentifier("autocrafting_preview_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewRequestPacket> STREAM_CODEC =
        StreamCodec
            .composite(
                UUIDUtil.STREAM_CODEC, AutocraftingPreviewRequestPacket::id,
                ResourceCodecs.STREAM_CODEC, AutocraftingPreviewRequestPacket::resource,
                ByteBufCodecs.VAR_LONG, AutocraftingPreviewRequestPacket::amount,
                AutocraftingPreviewRequestPacket::new
            );

    public static void handle(final AutocraftingPreviewRequestPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PreviewProvider provider) {
            final ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            provider.getPreview(packet.resource(), packet.amount(), new CancellationToken())
                .thenAccept(optionalPreview -> optionalPreview.ifPresent(preview ->
                    S2CPackets.sendAutocraftingPreviewResponse(player, packet.id, preview)));
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

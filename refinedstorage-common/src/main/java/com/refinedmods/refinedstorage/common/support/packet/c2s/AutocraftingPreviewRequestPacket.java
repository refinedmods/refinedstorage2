package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewStyle;
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
import static com.refinedmods.refinedstorage.common.util.PlatformUtil.enumStreamCodec;

public record AutocraftingPreviewRequestPacket(UUID id,
                                               PlatformResourceKey resource,
                                               long amount,
                                               AutocraftingPreviewStyle style) implements CustomPacketPayload {
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
                enumStreamCodec(AutocraftingPreviewStyle.values()), AutocraftingPreviewRequestPacket::style,
                AutocraftingPreviewRequestPacket::new
            );

    public static void handle(final AutocraftingPreviewRequestPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PreviewProvider provider) {
            final ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            handle(packet, provider, player);
        }
    }

    private static void handle(final AutocraftingPreviewRequestPacket packet, final PreviewProvider provider,
                               final ServerPlayer player) {
        if (packet.style == AutocraftingPreviewStyle.LIST) {
            provider.getPreview(packet.resource(), packet.amount(), new TimeoutableCancellationToken())
                .thenAccept(optionalPreview -> optionalPreview.ifPresent(preview ->
                    S2CPackets.sendAutocraftingPreviewResponse(player, packet.id, preview)));
        } else if (packet.style == AutocraftingPreviewStyle.TREE) {
            provider.getTreePreview(packet.resource(), packet.amount(), new TimeoutableCancellationToken())
                .thenAccept(optionalPreview -> optionalPreview.ifPresent(preview ->
                    S2CPackets.sendAutocraftingTreePreviewResponse(player, packet.id, preview)));
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

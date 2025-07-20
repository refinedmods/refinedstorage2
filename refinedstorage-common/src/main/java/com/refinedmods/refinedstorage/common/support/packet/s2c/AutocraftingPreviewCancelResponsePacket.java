package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingPreviewCancelResponsePacket() implements CustomPacketPayload {
    public static final Type<AutocraftingPreviewCancelResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_preview_cancel_response")
    );
    public static final AutocraftingPreviewCancelResponsePacket INSTANCE =
        new AutocraftingPreviewCancelResponsePacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewCancelResponsePacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    public static void handle() {
        ClientPlatformUtil.autocraftingPreviewCancelResponseReceived();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}


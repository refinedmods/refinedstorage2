package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record MessagePacket(Component title, Component component) implements CustomPacketPayload {
    public static final Type<MessagePacket> PACKET_TYPE = new Type<>(createIdentifier("message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePacket> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC, MessagePacket::title,
        ComponentSerialization.STREAM_CODEC, MessagePacket::component,
        MessagePacket::new
    );

    public static void handle(final MessagePacket packet) {
        ClientPlatformUtil.addMessageToast(packet.title, packet.component);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.platform.common.util.SecurityToastUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record NoPermissionPacket(Component component) implements CustomPacketPayload {
    public static final Type<NoPermissionPacket> PACKET_TYPE = new Type<>(createIdentifier("no_permission"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NoPermissionPacket> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC, NoPermissionPacket::component,
        NoPermissionPacket::new
    );

    public static void handle(final NoPermissionPacket packet) {
        SecurityToastUtil.addNoPermissionToast(packet.component);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

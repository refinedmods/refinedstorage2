package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record SetTenthAnniversaryCapePacket(boolean enabled) implements CustomPacketPayload {
    public static final Type<SetTenthAnniversaryCapePacket> PACKET_TYPE = new Type<>(
        createIdentifier("set_tenth_anniversary_cape")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetTenthAnniversaryCapePacket> STREAM_CODEC = StreamCodec
        .composite(ByteBufCodecs.BOOL, SetTenthAnniversaryCapePacket::enabled, SetTenthAnniversaryCapePacket::new);

    public static void handle(final SetTenthAnniversaryCapePacket packet, final PacketContext ctx) {
        Platform.INSTANCE.setTenthAnniversaryCape(ctx.getPlayer(), packet.enabled);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

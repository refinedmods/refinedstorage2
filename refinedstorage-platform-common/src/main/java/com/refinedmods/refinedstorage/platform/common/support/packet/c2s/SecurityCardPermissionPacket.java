package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.common.security.AbstractSecurityCardContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record SecurityCardPermissionPacket(PlatformPermission permission, boolean allowed)
    implements CustomPacketPayload {
    public static final Type<SecurityCardPermissionPacket> PACKET_TYPE = new Type<>(
        createIdentifier("security_card_permission")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardPermissionPacket> STREAM_CODEC = StreamCodec
        .composite(
            PlatformApi.INSTANCE.getPermissionRegistry().streamCodec(), SecurityCardPermissionPacket::permission,
            ByteBufCodecs.BOOL, SecurityCardPermissionPacket::allowed,
            SecurityCardPermissionPacket::new
        );

    public static void handle(final SecurityCardPermissionPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractSecurityCardContainerMenu securityCardContainerMenu) {
            securityCardContainerMenu.setPermission(packet.permission, packet.allowed);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

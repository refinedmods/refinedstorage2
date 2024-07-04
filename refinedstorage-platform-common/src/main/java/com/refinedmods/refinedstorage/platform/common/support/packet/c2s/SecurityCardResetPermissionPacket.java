package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.common.security.AbstractSecurityCardContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record SecurityCardResetPermissionPacket(PlatformPermission permission) implements CustomPacketPayload {
    public static final Type<SecurityCardResetPermissionPacket> PACKET_TYPE = new Type<>(
        createIdentifier("security_card_reset_permission")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardResetPermissionPacket> STREAM_CODEC =
        StreamCodec.composite(
            PlatformApi.INSTANCE.getPermissionRegistry().streamCodec(), SecurityCardResetPermissionPacket::permission,
            SecurityCardResetPermissionPacket::new
        );

    public static void handle(final SecurityCardResetPermissionPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractSecurityCardContainerMenu securityCardContainerMenu) {
            securityCardContainerMenu.resetPermissionServer(packet.permission);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

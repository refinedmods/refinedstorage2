package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record ResourceSlotChangePacket(int slotIndex, boolean tryAlternatives) implements CustomPacketPayload {
    public static final Type<ResourceSlotChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("resource_slot_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceSlotChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.INT, ResourceSlotChangePacket::slotIndex,
            ByteBufCodecs.BOOL, ResourceSlotChangePacket::tryAlternatives,
            ResourceSlotChangePacket::new
        );

    public static void handle(final ResourceSlotChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceSlotChange(packet.slotIndex, packet.tryAlternatives);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

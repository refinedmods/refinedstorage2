package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public record ResourceFilterSlotChangePacket(int slotIndex, PlatformResourceKey resource)
    implements CustomPacketPayload {
    public static final Type<ResourceFilterSlotChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("resource_filter_slot_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceFilterSlotChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.INT, ResourceFilterSlotChangePacket::slotIndex,
            ResourceCodecs.STREAM_CODEC, ResourceFilterSlotChangePacket::resource,
            ResourceFilterSlotChangePacket::new
        );

    public static void handle(final ResourceFilterSlotChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceFilterSlotUpdate(packet.slotIndex, requireNonNull(packet.resource));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

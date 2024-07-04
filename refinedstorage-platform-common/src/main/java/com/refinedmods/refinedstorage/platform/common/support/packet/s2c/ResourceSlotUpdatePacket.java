package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record ResourceSlotUpdatePacket(
    int slotIndex,
    Optional<ResourceAmount> resourceAmount
) implements CustomPacketPayload {
    public static final Type<ResourceSlotUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("resource_slot_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceSlotUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, ResourceSlotUpdatePacket::slotIndex,
            ByteBufCodecs.optional(ResourceCodecs.AMOUNT_STREAM_CODEC), ResourceSlotUpdatePacket::resourceAmount,
            ResourceSlotUpdatePacket::new
        );

    public static void handle(final ResourceSlotUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceSlotUpdate(packet.slotIndex, packet.resourceAmount.orElse(null));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

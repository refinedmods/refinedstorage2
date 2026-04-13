package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record PatternGridAllowedAlternativesUpdatePacket(int slotIndex, Set<Identifier> ids)
    implements CustomPacketPayload {
    public static final Type<PatternGridAllowedAlternativesUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_allowed_alternatives_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridAllowedAlternativesUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, PatternGridAllowedAlternativesUpdatePacket::slotIndex,
            ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC),
            PatternGridAllowedAlternativesUpdatePacket::ids,
            PatternGridAllowedAlternativesUpdatePacket::new
        );

    public static void handle(final PatternGridAllowedAlternativesUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu containerMenu) {
            containerMenu.handleAllowedAlternativesUpdate(packet.slotIndex, packet.ids);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

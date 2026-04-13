package com.refinedmods.refinedstorage.common.support.packet.c2s;

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

public record PatternGridAllowedAlternativesChangePacket(int slotIndex, Set<Identifier> ids)
    implements CustomPacketPayload {
    public static final Type<PatternGridAllowedAlternativesChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_allowed_alternatives_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridAllowedAlternativesChangePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, PatternGridAllowedAlternativesChangePacket::slotIndex,
            ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC),
            PatternGridAllowedAlternativesChangePacket::ids,
            PatternGridAllowedAlternativesChangePacket::new
        );

    public static void handle(final PatternGridAllowedAlternativesChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu containerMenu) {
            containerMenu.handleAllowedAlternativesUpdate(packet.slotIndex(), packet.ids());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

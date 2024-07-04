package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.PacketUtil.enumStreamCodec;

public record GridInsertPacket(GridInsertMode mode, boolean tryAlternatives) implements CustomPacketPayload {
    public static final Type<GridInsertPacket> PACKET_TYPE = new Type<>(createIdentifier("grid_insert"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridInsertPacket> STREAM_CODEC = StreamCodec.composite(
        enumStreamCodec(GridInsertMode.values()), GridInsertPacket::mode,
        ByteBufCodecs.BOOL, GridInsertPacket::tryAlternatives,
        GridInsertPacket::new
    );

    public static void handle(final GridInsertPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof GridInsertionStrategy strategy) {
            strategy.onInsert(packet.mode, packet.tryAlternatives);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

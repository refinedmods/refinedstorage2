package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.PacketUtil.enumStreamCodec;

public record GridScrollPacket(
    PlatformResourceKey resource,
    GridScrollMode mode,
    int slotIndex
) implements CustomPacketPayload {
    public static final Type<GridScrollPacket> PACKET_TYPE = new Type<>(createIdentifier("grid_scroll"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridScrollPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceCodecs.STREAM_CODEC, GridScrollPacket::resource,
        enumStreamCodec(GridScrollMode.values()), GridScrollPacket::mode,
        ByteBufCodecs.INT, GridScrollPacket::slotIndex,
        GridScrollPacket::new
    );

    public static void handle(final GridScrollPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof GridScrollingStrategy strategy) {
            strategy.onScroll(packet.resource, packet.mode, packet.slotIndex);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.PacketUtil.enumStreamCodec;

public record GridExtractPacket(
    PlatformResourceKey resource,
    GridExtractMode mode,
    boolean cursor
) implements CustomPacketPayload {
    public static final Type<GridExtractPacket> PACKET_TYPE = new Type<>(createIdentifier("grid_extract"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridExtractPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceCodecs.STREAM_CODEC, GridExtractPacket::resource,
        enumStreamCodec(GridExtractMode.values()), GridExtractPacket::mode,
        ByteBufCodecs.BOOL, GridExtractPacket::cursor,
        GridExtractPacket::new
    );

    public static void handle(final GridExtractPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof GridExtractionStrategy strategy) {
            strategy.onExtract(packet.resource, packet.mode, packet.cursor);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

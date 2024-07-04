package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class GridClearPacket implements CustomPacketPayload {
    public static final GridClearPacket INSTANCE = new GridClearPacket();
    public static final Type<GridClearPacket> PACKET_TYPE = new Type<>(createIdentifier("grid_clear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridClearPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private GridClearPacket() {
    }

    public static void handle(final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof AbstractGridContainerMenu grid) {
            grid.onClear();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

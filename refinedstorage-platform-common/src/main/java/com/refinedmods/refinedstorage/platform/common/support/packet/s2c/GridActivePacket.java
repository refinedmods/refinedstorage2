package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record GridActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<GridActivePacket> PACKET_TYPE = new Type<>(createIdentifier("grid_active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridActivePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, GridActivePacket::active,
        GridActivePacket::new
    );

    public static void handle(final GridActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof GridWatcher gridWatcher) {
            gridWatcher.onActiveChanged(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

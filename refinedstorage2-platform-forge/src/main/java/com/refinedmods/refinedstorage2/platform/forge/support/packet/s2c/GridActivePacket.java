package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridActivePacket(boolean active) implements CustomPacketPayload {
    public static GridActivePacket decode(final FriendlyByteBuf buf) {
        return new GridActivePacket(buf.readBoolean());
    }

    public static void handle(final GridActivePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            final AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof GridWatcher gridWatcher) {
                gridWatcher.onActiveChanged(packet.active);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(active);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_ACTIVE;
    }
}

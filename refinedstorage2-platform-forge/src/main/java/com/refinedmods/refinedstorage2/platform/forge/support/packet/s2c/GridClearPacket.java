package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class GridClearPacket implements CustomPacketPayload {
    public static void handle(final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            final AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof AbstractGridContainerMenu grid) {
                grid.onClear();
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf friendlyByteBuf) {
        // no op
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_CLEAR;
    }
}

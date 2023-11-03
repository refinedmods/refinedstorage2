package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridClearPacket {
    public static void handle(final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(GridClearPacket::handle));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractGridContainerMenu grid) {
            grid.onClear();
        }
    }
}

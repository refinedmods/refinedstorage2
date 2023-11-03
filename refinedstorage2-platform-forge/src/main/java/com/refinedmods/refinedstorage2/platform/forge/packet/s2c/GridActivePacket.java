package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridActivePacket {
    private final boolean active;

    public GridActivePacket(final boolean active) {
        this.active = active;
    }

    public static GridActivePacket decode(final FriendlyByteBuf buf) {
        return new GridActivePacket(buf.readBoolean());
    }

    public static void encode(final GridActivePacket packet, final FriendlyByteBuf buf) {
        buf.writeBoolean(packet.active);
    }

    public static void handle(final GridActivePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final GridActivePacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof GridWatcher gridWatcher) {
            gridWatcher.onActiveChanged(packet.active);
        }
    }
}

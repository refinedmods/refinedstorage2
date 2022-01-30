package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridActivePacket {
    private final boolean active;

    public GridActivePacket(boolean active) {
        this.active = active;
    }

    public static GridActivePacket decode(FriendlyByteBuf buf) {
        return new GridActivePacket(buf.readBoolean());
    }

    public static void encode(GridActivePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.active);
    }

    public static void handle(GridActivePacket packet, Supplier<NetworkEvent.Context> ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(GridActivePacket packet, Player player) {
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (screenHandler instanceof GridWatcher gridWatcher) {
            gridWatcher.onActiveChanged(packet.active);
        }
    }
}

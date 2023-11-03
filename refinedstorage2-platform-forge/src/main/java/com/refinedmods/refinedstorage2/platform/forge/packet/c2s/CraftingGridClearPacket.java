package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CraftingGridClearPacket {
    private final boolean toPlayerInventory;

    public CraftingGridClearPacket(final boolean toPlayerInventory) {
        this.toPlayerInventory = toPlayerInventory;
    }

    public static CraftingGridClearPacket decode(final FriendlyByteBuf buf) {
        return new CraftingGridClearPacket(buf.readBoolean());
    }

    public static void encode(final CraftingGridClearPacket packet, final FriendlyByteBuf buf) {
        buf.writeBoolean(packet.toPlayerInventory);
    }

    public static void handle(final CraftingGridClearPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof CraftingGridContainerMenu craftingGridMenu) {
            ctx.get().enqueueWork(() -> craftingGridMenu.clear(packet.toPlayerInventory));
        }
        ctx.get().setPacketHandled(true);
    }
}

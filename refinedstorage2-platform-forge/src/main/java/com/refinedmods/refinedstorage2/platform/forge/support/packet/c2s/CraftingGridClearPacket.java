package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CraftingGridClearPacket(boolean toPlayerInventory) implements CustomPacketPayload {
    public static CraftingGridClearPacket decode(final FriendlyByteBuf buf) {
        return new CraftingGridClearPacket(buf.readBoolean());
    }

    public static void handle(final CraftingGridClearPacket packet, final PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof CraftingGridContainerMenu craftingGridContainerMenu) {
                craftingGridContainerMenu.clear(packet.toPlayerInventory());
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(toPlayerInventory);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.CRAFTING_GRID_CLEAR;
    }
}

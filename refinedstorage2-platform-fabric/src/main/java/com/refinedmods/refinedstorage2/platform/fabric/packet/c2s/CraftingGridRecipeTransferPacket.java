package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CraftingGridRecipeTransferPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final int slots = buf.readInt();
        final List<List<ItemResource>> recipe = new ArrayList<>(slots);
        for (int i = 0; i < slots; ++i) {
            final int slotPossibilitiesCount = buf.readInt();
            final List<ItemResource> slotPossibilities = new ArrayList<>(slotPossibilitiesCount);
            for (int j = 0; j < slotPossibilitiesCount; ++j) {
                slotPossibilities.add(PacketUtil.readItemResource(buf));
            }
            recipe.add(slotPossibilities);
        }
        handle(recipe, player, server);
    }

    private void handle(final List<List<ItemResource>> recipe, final Player player, final MinecraftServer server) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof CraftingGridContainerMenu craftingGridContainerMenu) {
            server.execute(() -> craftingGridContainerMenu.transferRecipe(recipe));
        }
    }
}

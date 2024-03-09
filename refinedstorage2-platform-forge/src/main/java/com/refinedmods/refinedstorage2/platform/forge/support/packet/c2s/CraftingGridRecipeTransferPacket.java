package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CraftingGridRecipeTransferPacket(List<List<ItemResource>> recipe) implements CustomPacketPayload {
    public static CraftingGridRecipeTransferPacket decode(final FriendlyByteBuf buf) {
        final int slots = buf.readInt();
        final List<List<ItemResource>> recipe = new ArrayList<>(slots);
        for (int i = 0; i < slots; ++i) {
            final int slotPossibilityCount = buf.readInt();
            final List<ItemResource> slotPossibilities = new ArrayList<>(slotPossibilityCount);
            for (int j = 0; j < slotPossibilityCount; ++j) {
                slotPossibilities.add(PacketUtil.readItemResource(buf));
            }
            recipe.add(slotPossibilities);
        }
        return new CraftingGridRecipeTransferPacket(recipe);
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(recipe.size());
        for (final List<ItemResource> slotPossibilities : recipe) {
            buf.writeInt(slotPossibilities.size());
            for (final ItemResource slotPossibility : slotPossibilities) {
                slotPossibility.toBuffer(buf);
            }
        }
    }

    public static void handle(final CraftingGridRecipeTransferPacket packet, final PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof CraftingGridContainerMenu craftingGridContainerMenu) {
                craftingGridContainerMenu.transferRecipe(packet.recipe());
            }
        }));
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.CRAFTING_GRID_RECIPE_TRANSFER;
    }
}

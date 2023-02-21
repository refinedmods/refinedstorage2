package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

public interface CraftingGridSource {
    CraftingMatrix getCraftingMatrix();

    ResultContainer getCraftingResult();

    NonNullList<ItemStack> getRemainingItems(Player player);

    CraftingGridRefillContext openRefillContext();

    CraftingGridRefillContext openSnapshotRefillContext(Player player);

    ItemStack insert(ItemStack stack, Player player);

    void clearMatrix(Player player, boolean toPlayerInventory);
}

package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

class ClientCraftingGridSource implements CraftingGridSource {
    private final CraftingMatrix craftingMatrix;
    private final ResultContainer craftingResult;

    ClientCraftingGridSource() {
        this.craftingMatrix = new CraftingMatrix(null);
        this.craftingResult = new ResultContainer();
    }

    @Override
    public CraftingMatrix getCraftingMatrix() {
        return craftingMatrix;
    }

    @Override
    public ResultContainer getCraftingResult() {
        return craftingResult;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CraftingGridRefillContext openRefillContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CraftingGridRefillContext openSnapshotRefillContext(final Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        C2SPackets.sendCraftingGridClear(toPlayerInventory);
        return true;
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        C2SPackets.sendCraftingGridRecipeTransfer(recipe);
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        throw new UnsupportedOperationException();
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

public class ClientCraftingGridSource implements CraftingGridSource {
    private final CraftingMatrix craftingMatrix;
    private final ResultContainer craftingResult;

    public ClientCraftingGridSource() {
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
    public NonNullList<ItemStack> getRemainingItems(final Player player) {
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
        Platform.INSTANCE.getClientToServerCommunications().sendCraftingGridClear(toPlayerInventory);
        return true;
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        Platform.INSTANCE.getClientToServerCommunications().sendCraftingGridRecipeTransfer(recipe);
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        throw new UnsupportedOperationException();
    }
}

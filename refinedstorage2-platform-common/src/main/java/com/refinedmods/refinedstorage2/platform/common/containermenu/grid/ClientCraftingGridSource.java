package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;

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
    public ItemStack insert(final ItemStack stack, final Player player) {
        throw new UnsupportedOperationException();
    }
}

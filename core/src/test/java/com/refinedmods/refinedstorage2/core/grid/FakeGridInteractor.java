package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FakeGridInteractor implements GridInteractor {
    private ItemStack cursorStack = ItemStack.EMPTY;
    private List<ItemStack> inventory = new ArrayList<>();

    @Override
    public ItemStack getCursorStack() {
        return cursorStack;
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        this.cursorStack = stack;
    }

    @Override
    public ItemStack insertIntoInventory(ItemStack stack) {
        inventory.add(stack);
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }
}

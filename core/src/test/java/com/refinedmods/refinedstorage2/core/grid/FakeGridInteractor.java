package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.Collection;

public class FakeGridInteractor implements GridInteractor {
    private ItemStack cursorStack = ItemStack.EMPTY;
    private StorageDisk<ItemStack> inventory = new ItemDiskStorage(1000);

    public void resetInventoryAndSetCapacity(int capacity) {
        inventory = new ItemDiskStorage(capacity);
    }

    @Override
    public ItemStack getCursorStack() {
        return cursorStack;
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        this.cursorStack = stack;
    }

    @Override
    public ItemStack insertIntoInventory(ItemStack stack, int preferredSlot) {
        return inventory.insert(stack, stack.getCount(), Action.EXECUTE).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack extractFromInventory(ItemStack template, int slot, int count) {
        return inventory.extract(template, count, Action.EXECUTE).orElse(ItemStack.EMPTY);
    }

    public Collection<ItemStack> getInventory() {
        return inventory.getStacks();
    }
}

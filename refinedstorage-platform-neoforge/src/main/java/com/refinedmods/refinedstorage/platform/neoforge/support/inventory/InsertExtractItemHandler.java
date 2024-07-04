package com.refinedmods.refinedstorage.platform.neoforge.support.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class InsertExtractItemHandler implements IItemHandler {
    private final IItemHandler insertHandler;
    private final IItemHandler extractHandler;

    public InsertExtractItemHandler(final IItemHandler insertHandler, final IItemHandler extractHandler) {
        this.insertHandler = insertHandler;
        this.extractHandler = extractHandler;
    }

    @Override
    public int getSlots() {
        return insertHandler.getSlots() + extractHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(final int slot) {
        return slot < insertHandler.getSlots()
            ? insertHandler.getStackInSlot(slot)
            : extractHandler.getStackInSlot(slot - insertHandler.getSlots());
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
        return slot < insertHandler.getSlots() ? insertHandler.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        return slot >= insertHandler.getSlots()
            ? extractHandler.extractItem(slot - insertHandler.getSlots(), amount, simulate)
            : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return slot < insertHandler.getSlots()
            ? insertHandler.getSlotLimit(slot)
            : extractHandler.getSlotLimit(slot - insertHandler.getSlots());
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        return slot < insertHandler.getSlots()
            ? insertHandler.isItemValid(slot, stack)
            : extractHandler.isItemValid(slot - extractHandler.getSlots(), stack);
    }
}

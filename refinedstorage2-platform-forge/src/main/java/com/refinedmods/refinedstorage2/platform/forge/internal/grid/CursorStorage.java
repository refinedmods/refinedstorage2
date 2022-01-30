package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class CursorStorage implements IItemHandler {
    private final AbstractContainerMenu containerMenu;

    public CursorStorage(AbstractContainerMenu containerMenu) {
        this.containerMenu = containerMenu;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return containerMenu.getCarried();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0) {
            return stack;
        }
        if (containerMenu.getCarried().isEmpty()) {
            return insertIntoEmptyCursor(stack, simulate);
        }
        if (!isSame(containerMenu.getCarried(), stack)) {
            return stack;
        }
        return insertIntoCursorWithExistingContent(stack, simulate);
    }

    private ItemStack insertIntoEmptyCursor(ItemStack itemStack, boolean simulate) {
        if (!simulate) {
            containerMenu.setCarried(itemStack);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack insertIntoCursorWithExistingContent(ItemStack itemStack, boolean simulate) {
        int spaceLeft = containerMenu.getCarried().getMaxStackSize() - containerMenu.getCarried().getCount();
        if (spaceLeft <= 0) {
            return itemStack;
        }
        int toInsert = Math.min(itemStack.getCount(), spaceLeft);
        int remainder = itemStack.getCount() - toInsert;
        if (!simulate) {
            containerMenu.getCarried().grow(toInsert);
        }
        return ItemHandlerHelper.copyStackWithSize(itemStack, remainder);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        if (containerMenu.getCarried().isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(containerMenu.getCarried().getCount(), amount);
        ItemStack extractedStack = ItemHandlerHelper.copyStackWithSize(containerMenu.getCarried(), extracted);
        if (!simulate) {
            containerMenu.getCarried().shrink(extracted);
        }
        return extractedStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0;
    }

    private boolean isSame(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}

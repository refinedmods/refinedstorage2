package com.refinedmods.refinedstorage2.platform.forge.grid.strategy;

import javax.annotation.Nonnull;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

class CursorItemHandler implements IItemHandler {
    private final AbstractContainerMenu containerMenu;

    CursorItemHandler(final AbstractContainerMenu containerMenu) {
        this.containerMenu = containerMenu;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(final int slot) {
        return containerMenu.getCarried();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
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

    private ItemStack insertIntoEmptyCursor(final ItemStack itemStack, final boolean simulate) {
        if (!simulate) {
            containerMenu.setCarried(itemStack);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack insertIntoCursorWithExistingContent(final ItemStack itemStack, final boolean simulate) {
        final int spaceLeft = containerMenu.getCarried().getMaxStackSize() - containerMenu.getCarried().getCount();
        if (spaceLeft <= 0) {
            return itemStack;
        }
        final int toInsert = Math.min(itemStack.getCount(), spaceLeft);
        final int remainder = itemStack.getCount() - toInsert;
        if (!simulate) {
            containerMenu.getCarried().grow(toInsert);
        }
        return ItemHandlerHelper.copyStackWithSize(itemStack, remainder);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        if (containerMenu.getCarried().isEmpty()) {
            return ItemStack.EMPTY;
        }
        final int extracted = Math.min(containerMenu.getCarried().getCount(), amount);
        final ItemStack extractedStack = ItemHandlerHelper.copyStackWithSize(containerMenu.getCarried(), extracted);
        if (!simulate) {
            containerMenu.getCarried().shrink(extracted);
        }
        return extractedStack;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        return slot == 0;
    }

    private boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}

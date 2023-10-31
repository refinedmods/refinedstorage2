package com.refinedmods.refinedstorage2.platform.common.grid;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class CraftingMatrixContainerMenu extends AbstractContainerMenu {
    @Nullable
    private final Runnable listener;

    public CraftingMatrixContainerMenu(@Nullable final Runnable listener) {
        super(MenuType.CRAFTING, 0);
        this.listener = listener;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return false;
    }

    @Override
    public void slotsChanged(final Container container) {
        if (listener != null) {
            listener.run();
        }
    }
}

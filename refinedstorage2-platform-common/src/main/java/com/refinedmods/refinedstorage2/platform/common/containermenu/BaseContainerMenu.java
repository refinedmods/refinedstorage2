package com.refinedmods.refinedstorage2.platform.common.containermenu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BaseContainerMenu extends AbstractContainerMenu {
    protected BaseContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
    }

    protected void resetSlots() {
        slots.clear();
    }

    protected void addPlayerInventory(final Inventory inventory, final int xInventory, final int yInventory) {
        int id = 9;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(inventory, id++, xInventory + x * 18, yInventory + y * 18));
            }
        }

        id = 0;
        for (int i = 0; i < 9; i++) {
            final int x = xInventory + i * 18;
            final int y = yInventory + 4 + (3 * 18);
            addSlot(new Slot(inventory, id++, x, y));
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }
}

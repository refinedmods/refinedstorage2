package com.refinedmods.refinedstorage2.platform.fabric.containermenu;

import com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot.FilterSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BaseContainerMenu extends AbstractContainerMenu {
    protected BaseContainerMenu(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventory(Inventory inventory, int xInventory, int yInventory) {
        int id = 9;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(inventory, id++, xInventory + x * 18, yInventory + y * 18));
            }
        }

        id = 0;

        for (int i = 0; i < 9; i++) {
            int x = xInventory + i * 18;
            int y = yInventory + 4 + (3 * 18);

            addSlot(new Slot(inventory, id++, x, y));
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof FilterSlot);
    }

    @Override
    public void clicked(int id, int dragType, ClickType actionType, Player player) {
        Slot slot = id >= 0 ? getSlot(id) : null;

        if (slot instanceof FilterSlot) {
            ItemStack cursorStack = getCarried();
            if (cursorStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.set(cursorStack.copy());
            }
        } else {
            super.clicked(id, dragType, actionType, player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}

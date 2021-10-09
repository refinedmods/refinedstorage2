package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class FilterSlot extends Slot {
    public FilterSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        super.setStack(stack);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
}

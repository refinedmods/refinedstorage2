package com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilterSlot extends Slot {
    public FilterSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        super.set(stack);
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return false;
    }
}

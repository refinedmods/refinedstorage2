package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.slot;

import java.util.function.Predicate;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ValidatedSlot extends Slot {
    private final Predicate<ItemStack> predicate;

    public ValidatedSlot(Inventory inventory, int index, int x, int y, Predicate<ItemStack> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return predicate.test(stack);
    }
}

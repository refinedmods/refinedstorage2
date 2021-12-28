package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ValidatedSlot extends Slot {
    private final Predicate<ItemStack> predicate;

    public ValidatedSlot(Container inventory, int index, int x, int y, Predicate<ItemStack> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return predicate.test(stack);
    }
}

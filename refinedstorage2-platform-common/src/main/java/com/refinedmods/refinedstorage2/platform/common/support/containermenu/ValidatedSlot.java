package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ValidatedSlot extends Slot {
    private final Predicate<ItemStack> predicate;

    public ValidatedSlot(final Container container,
                         final int index,
                         final int x,
                         final int y,
                         final Predicate<ItemStack> predicate) {
        super(container, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return predicate.test(stack);
    }
}

package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItem;

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

    public static Slot forStorageContainer(final Container container,
                                           final int index,
                                           final int x,
                                           final int y) {
        return new ValidatedSlot(container, index, x, y, stack -> stack.getItem() instanceof StorageContainerItem);
    }
}

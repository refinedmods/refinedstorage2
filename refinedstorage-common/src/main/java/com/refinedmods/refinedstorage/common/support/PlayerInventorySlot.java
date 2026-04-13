package com.refinedmods.refinedstorage.common.support;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

class PlayerInventorySlot extends Slot {
    @Nullable
    private final PlayerInventoryListener listener;

    PlayerInventorySlot(final Container container,
                        final int slot,
                        final int x,
                        final int y,
                        @Nullable final PlayerInventoryListener listener) {
        super(container, slot, x, y);
        this.listener = listener;
    }

    @Override
    public void set(final ItemStack stack) {
        if (listener != null) {
            listener.changed(getItem(), stack);
        }
        super.set(stack);
    }

    @Override
    public ItemStack remove(final int amount) {
        if (listener != null) {
            final ItemStack before = getItem().copy();
            final ItemStack result = super.remove(amount);
            listener.changed(before, getItem());
            return result;
        }
        return super.remove(amount);
    }
}

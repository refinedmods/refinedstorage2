package com.refinedmods.refinedstorage2.platform.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class ContainerUtil {
    private ContainerUtil() {
    }

    public static CompoundTag write(final Container container) {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            final ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                tag.put(getSlotKey(i), stack.save(new CompoundTag()));
            }
        }
        return tag;
    }

    public static void read(final CompoundTag tag, final Container container) {
        for (int i = 0; i < container.getContainerSize(); ++i) {
            if (hasItemInSlot(tag, i)) {
                final CompoundTag stackTag = tag.getCompound(getSlotKey(i));
                readSlot(container, i, stackTag);
            }
        }
    }

    private static String getSlotKey(final int slot) {
        return "i" + slot;
    }

    public static boolean hasItemInSlot(final CompoundTag tag, final int slot) {
        return tag.contains(getSlotKey(slot));
    }

    private static void readSlot(final Container container, final int slot, final CompoundTag stackTag) {
        final ItemStack stack = ItemStack.of(stackTag);
        if (!stack.isEmpty()) {
            container.setItem(slot, stack);
        }
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.fluid;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.nbt.ListTag;

public class FluidFilterInventory {
    private final FluidVariant[] slots;
    private final Runnable listener;

    public FluidFilterInventory(int size) {
        this(size, () -> {
        });
    }

    public FluidFilterInventory(int size, Runnable listener) {
        this.slots = new FluidVariant[size];
        this.listener = listener;
        for (int i = 0; i < size; ++i) {
            slots[i] = FluidVariant.blank();
        }
    }

    public void setSlot(int slot, FluidVariant fluidVariant) {
        Preconditions.checkNotNull(fluidVariant);
        slots[slot] = fluidVariant;
        listener.run();
    }

    public FluidVariant getSlot(int slot) {
        return slots[slot];
    }

    public void readList(ListTag nbtList) {
        for (int i = 0; i < nbtList.size(); ++i) {
            FluidVariant fluidVariant = FluidVariant.fromNbt(nbtList.getCompound(i));
            slots[i] = fluidVariant;
        }
    }

    public ListTag toList() {
        ListTag list = new ListTag();
        for (int i = 0; i < slots.length; ++i) {
            FluidVariant fluidVariant = getSlot(i);
            list.add(fluidVariant.toNbt());
        }
        return list;
    }
}

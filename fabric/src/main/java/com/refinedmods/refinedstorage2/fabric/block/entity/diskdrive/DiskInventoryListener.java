package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import alexiil.mc.lib.attributes.item.FixedItemInvView;
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener;
import net.minecraft.item.ItemStack;

public class DiskInventoryListener implements ItemInvSlotChangeListener {
    private final DiskDriveBlockEntity diskDrive;

    public DiskInventoryListener(DiskDriveBlockEntity diskDrive) {
        this.diskDrive = diskDrive;
    }

    @Override
    public void onChange(FixedItemInvView view, int slot, ItemStack oldStack, ItemStack newStack) {
        if (diskDrive.getWorld().isClient()) {
            return;
        }

        diskDrive.onDiskChanged(slot);
    }
}

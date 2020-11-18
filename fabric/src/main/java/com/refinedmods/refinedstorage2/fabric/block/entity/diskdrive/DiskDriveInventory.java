package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import com.refinedmods.refinedstorage2.fabric.item.StorageDiskItem;
import net.minecraft.item.ItemStack;

public class DiskDriveInventory extends FullFixedItemInv {
    private static final ItemFilter DISK_FILTER = new ItemClassFilter(StorageDiskItem.class);

    public DiskDriveInventory() {
        super(8);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item) {
        return DISK_FILTER.matches(item);
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return DISK_FILTER;
    }
}

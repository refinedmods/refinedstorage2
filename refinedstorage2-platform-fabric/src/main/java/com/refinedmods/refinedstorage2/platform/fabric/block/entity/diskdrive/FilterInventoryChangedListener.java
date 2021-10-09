package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;

public class FilterInventoryChangedListener implements InventoryChangedListener {
    private final DiskDriveBlockEntity diskDrive;

    public FilterInventoryChangedListener(DiskDriveBlockEntity diskDrive) {
        this.diskDrive = diskDrive;
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        if (diskDrive.getWorld().isClient()) {
            return;
        }

        List<ItemStack> filterTemplates = new ArrayList<>();
        for (int i = 0; i < sender.size(); ++i) {
            ItemStack filter = sender.getStack(i);
            if (!filter.isEmpty()) {
                filterTemplates.add(filter);
            }
        }

        diskDrive.setFilterTemplates(filterTemplates);
    }
}

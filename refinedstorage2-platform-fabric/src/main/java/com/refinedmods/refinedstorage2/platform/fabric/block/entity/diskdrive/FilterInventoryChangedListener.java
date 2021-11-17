package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class FilterInventoryChangedListener implements ContainerListener {
    private final DiskDriveBlockEntity diskDrive;

    public FilterInventoryChangedListener(DiskDriveBlockEntity diskDrive) {
        this.diskDrive = diskDrive;
    }

    @Override
    public void containerChanged(Container sender) {
        if (diskDrive.getLevel() == null || diskDrive.getLevel().isClientSide()) {
            return;
        }

        List<ItemStack> filterTemplates = new ArrayList<>();
        for (int i = 0; i < sender.getContainerSize(); ++i) {
            ItemStack filter = sender.getItem(i);
            if (!filter.isEmpty()) {
                filterTemplates.add(filter);
            }
        }

        diskDrive.setFilterTemplates(filterTemplates);
    }
}

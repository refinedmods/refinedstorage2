package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import java.util.List;
import java.util.stream.StreamSupport;

import alexiil.mc.lib.attributes.item.FixedItemInvView;
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener;
import net.minecraft.item.ItemStack;

public class FilterInventoryListener implements ItemInvSlotChangeListener {
    private final DiskDriveBlockEntity diskDrive;

    public FilterInventoryListener(DiskDriveBlockEntity diskDrive) {
        this.diskDrive = diskDrive;
    }

    @Override
    public void onChange(FixedItemInvView view, int slot, ItemStack oldStack, ItemStack newStack) {
        if (diskDrive.getWorld().isClient()) {
            return;
        }

        List<ItemStack> filterTemplates = StreamSupport
                .stream(view.stackIterable().spliterator(), false)
                .filter(s -> !s.isEmpty())
                .toList();

        diskDrive.setFilterTemplates(filterTemplates);
        diskDrive.markDirty();
    }
}

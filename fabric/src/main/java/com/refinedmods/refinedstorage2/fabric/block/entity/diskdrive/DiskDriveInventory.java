package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.StorageDiskProvider;
import com.refinedmods.refinedstorage2.fabric.item.StorageDiskItem;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class DiskDriveInventory extends FullFixedItemInv implements StorageDiskProvider {
    private static final ItemFilter DISK_FILTER = new ItemClassFilter(StorageDiskItem.class);

    public DiskDriveInventory() {
        super(DiskDriveNetworkNode.DISK_COUNT);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return DISK_FILTER.matches(stack);
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return DISK_FILTER;
    }

    @Override
    public Optional<UUID> getDiskId(int slot) {
        ItemStack stack = getInvStack(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageDiskItem)) {
            return Optional.empty();
        }

        return StorageDiskItem.getId(stack);
    }
}

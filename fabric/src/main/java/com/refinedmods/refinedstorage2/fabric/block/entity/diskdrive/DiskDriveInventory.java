package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskProvider;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.StorageDiskItem;

import java.util.Optional;
import java.util.UUID;

import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import net.minecraft.item.ItemStack;

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
        return validateAndGetStack(slot).flatMap(stack -> ((StorageDiskItem) stack.getItem()).getDiskId(stack));
    }

    @Override
    public Optional<StorageChannelType<?>> getStorageChannelType(int slot) {
        return validateAndGetStack(slot).flatMap(stack -> ((StorageDiskItem) stack.getItem()).getType(stack));
    }

    private Optional<ItemStack> validateAndGetStack(int slot) {
        ItemStack stack = getInvStack(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageDiskItem)) {
            return Optional.empty();
        }
        return Optional.of(stack);
    }
}

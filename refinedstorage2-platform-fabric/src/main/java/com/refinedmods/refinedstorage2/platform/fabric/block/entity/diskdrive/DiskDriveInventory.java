package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskProvider;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class DiskDriveInventory extends SimpleInventory implements StorageDiskProvider {
    private final DiskDriveBlockEntity diskDrive;

    public DiskDriveInventory(DiskDriveBlockEntity diskDrive) {
        super(DiskDriveNetworkNode.DISK_COUNT);
        this.diskDrive = diskDrive;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return super.canInsert(stack) && stack.getItem() instanceof StorageDiskItem;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        if (diskDrive.getWorld() == null || diskDrive.getWorld().isClient()) {
            return;
        }
        diskDrive.onDiskChanged(slot);
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
        ItemStack stack = getStack(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageDiskItem)) {
            return Optional.empty();
        }
        return Optional.of(stack);
    }
}

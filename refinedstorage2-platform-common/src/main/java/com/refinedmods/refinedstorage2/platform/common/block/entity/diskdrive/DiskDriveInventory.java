package com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskProvider;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.item.StorageDiskItem;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class DiskDriveInventory extends SimpleContainer implements StorageDiskProvider {
    private final DiskDriveBlockEntity diskDrive;

    public DiskDriveInventory(DiskDriveBlockEntity diskDrive) {
        super(DiskDriveNetworkNode.DISK_COUNT);
        this.diskDrive = diskDrive;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof StorageDiskItem;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        // level will not yet be present
        boolean isJustPlacedIntoLevelOrLoading = diskDrive.getLevel() == null || diskDrive.getLevel().isClientSide();
        // level will be present, but network not yet
        boolean isPlacedThroughDismantlingMode = diskDrive.getNode().getNetwork() == null;
        if (isJustPlacedIntoLevelOrLoading || isPlacedThroughDismantlingMode) {
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
        ItemStack stack = getItem(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageDiskItem)) {
            return Optional.empty();
        }
        return Optional.of(stack);
    }
}

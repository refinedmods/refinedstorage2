package com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.StorageDiskProvider;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.item.StorageDiskItem;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class DiskDriveInventory extends SimpleContainer implements StorageDiskProvider {
    private final AbstractDiskDriveBlockEntity diskDrive;
    @Nullable
    private StorageRepository storageRepository;

    public DiskDriveInventory(final AbstractDiskDriveBlockEntity diskDrive, final int diskCount) {
        super(diskCount);
        this.diskDrive = diskDrive;
    }

    public void setStorageRepository(@Nullable final StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return stack.getItem() instanceof StorageDiskItem;
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        // Forge InvWrapper calls this instead of setItem.
        final ItemStack result = super.removeItem(slot, amount);
        diskDrive.onDiskChanged(slot);
        return result;
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        // level will not yet be present
        final boolean isJustPlacedIntoLevelOrLoading = diskDrive.getLevel() == null
            || diskDrive.getLevel().isClientSide();
        // level will be present, but network not yet
        final boolean isPlacedThroughDismantlingMode = diskDrive.getNode().getNetwork() == null;
        if (isJustPlacedIntoLevelOrLoading || isPlacedThroughDismantlingMode) {
            return;
        }
        diskDrive.onDiskChanged(slot);
    }

    @Override
    public Optional<TypedStorage<?>> resolve(final int slot) {
        if (storageRepository == null) {
            return Optional.empty();
        }
        return validateAndGetStack(slot).flatMap(stack -> ((StorageDiskItem) stack.getItem()).resolve(
            storageRepository,
            stack
        ));
    }

    private Optional<ItemStack> validateAndGetStack(final int slot) {
        final ItemStack stack = getItem(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageDiskItem)) {
            return Optional.empty();
        }
        return Optional.of(stack);
    }
}

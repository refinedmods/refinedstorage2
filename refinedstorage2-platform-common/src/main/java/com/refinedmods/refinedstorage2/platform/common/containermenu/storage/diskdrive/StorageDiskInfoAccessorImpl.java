package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerItem;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StorageDiskInfoAccessorImpl implements StorageDiskInfoAccessor {
    private final Level level;

    public StorageDiskInfoAccessorImpl(final Level level) {
        this.level = level;
    }

    @Override
    public Optional<StorageInfo> getInfo(final ItemStack stack) {
        if (stack.getItem() instanceof StorageContainerItem storageContainerItem) {
            return storageContainerItem.getInfo(level, stack);
        }
        return Optional.empty();
    }

    @Override
    public boolean hasStacking(final ItemStack stack) {
        if (stack.getItem() instanceof StorageContainerItem storageContainerItem) {
            return storageContainerItem.hasStacking();
        }
        return false;
    }
}

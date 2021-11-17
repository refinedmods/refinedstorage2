package com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StorageInfoAccessorImpl implements StorageInfoAccessor {
    private final Level level;

    public StorageInfoAccessorImpl(Level level) {
        this.level = level;
    }

    @Override
    public Optional<StorageInfo> getInfo(ItemStack stack) {
        if (stack.getItem() instanceof StorageDiskItem storageDiskItem) {
            return storageDiskItem.getInfo(level, stack);
        }
        return Optional.empty();
    }
}

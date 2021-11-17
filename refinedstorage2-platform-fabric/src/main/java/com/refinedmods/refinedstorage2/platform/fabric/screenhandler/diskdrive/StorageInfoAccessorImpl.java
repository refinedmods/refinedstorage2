package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StorageInfoAccessorImpl implements StorageInfoAccessor {
    private final Level world;

    public StorageInfoAccessorImpl(Level world) {
        this.world = world;
    }

    @Override
    public Optional<StorageInfo> getInfo(ItemStack stack) {
        if (stack.getItem() instanceof StorageDiskItem storageDiskItem) {
            return storageDiskItem.getInfo(world, stack);
        }
        return Optional.empty();
    }
}

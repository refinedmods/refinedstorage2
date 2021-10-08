package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StorageInfoAccessorImpl implements StorageInfoAccessor {
    private final World world;

    public StorageInfoAccessorImpl(World world) {
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

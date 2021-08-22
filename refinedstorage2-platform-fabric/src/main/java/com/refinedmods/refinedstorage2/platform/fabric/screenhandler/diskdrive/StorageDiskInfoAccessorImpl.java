package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskItem;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StorageDiskInfoAccessorImpl implements StorageDiskInfoAccessor {
    private final World world;

    public StorageDiskInfoAccessorImpl(World world) {
        this.world = world;
    }

    @Override
    public Optional<StorageDiskInfo> getDiskInfo(ItemStack stack) {
        if (stack.getItem() instanceof StorageDiskItem storageDiskItem) {
            return storageDiskItem.getInfo(world, stack);
        }
        return Optional.empty();
    }
}

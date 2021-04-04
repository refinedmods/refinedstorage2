package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.fabric.item.StorageDiskItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StorageDiskInfoAccessorImpl implements StorageDiskInfoAccessor {
    private final World world;

    public StorageDiskInfoAccessorImpl(World world) {
        this.world = world;
    }

    @Override
    public Optional<StorageDiskInfo> getDiskInfo(ItemStack stack) {
        return StorageDiskItem.getInfo(world, stack);
    }
}

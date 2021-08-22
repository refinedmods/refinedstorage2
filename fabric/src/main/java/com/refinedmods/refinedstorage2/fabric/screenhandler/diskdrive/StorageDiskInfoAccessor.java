package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskInfo;

import java.util.Optional;

import net.minecraft.item.ItemStack;

public interface StorageDiskInfoAccessor {
    Optional<StorageDiskInfo> getDiskInfo(ItemStack stack);
}

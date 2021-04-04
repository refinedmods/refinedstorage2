package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import net.minecraft.item.ItemStack;

public interface StorageDiskInfoAccessor {
    Optional<StorageDiskInfo> getDiskInfo(ItemStack stack);
}

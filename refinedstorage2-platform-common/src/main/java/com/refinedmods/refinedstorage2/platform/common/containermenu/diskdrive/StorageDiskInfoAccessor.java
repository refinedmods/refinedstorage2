package com.refinedmods.refinedstorage2.platform.common.containermenu.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StorageDiskInfoAccessor {
    Optional<StorageInfo> getInfo(ItemStack stack);
}

package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

interface StorageDiskInfoAccessor {
    Optional<StorageInfo> getInfo(ItemStack stack);
}

package com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public interface StorageInfoAccessor {
    Optional<StorageInfo> getInfo(ItemStack stack);
}

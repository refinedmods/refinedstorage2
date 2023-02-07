package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class EmptyStorageDiskInfoAccessor implements StorageDiskInfoAccessor {
    @Override
    public Optional<StorageInfo> getInfo(final ItemStack stack) {
        return Optional.empty();
    }
}

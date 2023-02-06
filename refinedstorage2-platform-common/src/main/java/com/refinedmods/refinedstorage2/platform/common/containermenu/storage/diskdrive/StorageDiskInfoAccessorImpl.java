package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class StorageDiskInfoAccessorImpl implements StorageDiskInfoAccessor {
    private final StorageRepository storageRepository;

    public StorageDiskInfoAccessorImpl(final StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public Optional<StorageInfo> getInfo(final ItemStack stack) {
        if (stack.getItem() instanceof StorageContainerItem storageContainerItem) {
            return storageContainerItem.getInfo(storageRepository, stack);
        }
        return Optional.empty();
    }
}

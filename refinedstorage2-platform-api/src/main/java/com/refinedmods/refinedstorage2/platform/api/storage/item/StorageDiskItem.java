package com.refinedmods.refinedstorage2.platform.api.storage.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

// TODO: Rename to StorageContainerItem
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageDiskItem {
    Optional<TypedStorage<?>> resolve(StorageRepository storageRepository, ItemStack stack);

    Optional<StorageInfo> getInfo(Level level, ItemStack stack);

    boolean hasStacking();
}

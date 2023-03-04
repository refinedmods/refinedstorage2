package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageContainerItem {
    Optional<TypedStorage<?>> resolve(StorageRepository storageRepository, ItemStack stack);

    Optional<StorageInfo> getInfo(StorageRepository storageRepository, ItemStack stack);
}

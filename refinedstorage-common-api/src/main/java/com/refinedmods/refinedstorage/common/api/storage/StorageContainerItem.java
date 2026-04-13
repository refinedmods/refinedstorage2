package com.refinedmods.refinedstorage.common.api.storage;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageContainerItem {
    Predicate<ItemStack> VALIDATOR = stack -> stack.getItem() instanceof StorageContainerItem;

    Optional<SerializableStorage> resolve(StorageRepository storageRepository, ItemStack stack);

    Optional<StorageInfo> getInfo(StorageRepository storageRepository, ItemStack stack);
}

package com.refinedmods.refinedstorage2.platform.api.storage.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageDiskItem {
    Optional<StorageChannelType<?>> getType(ItemStack stack);

    Optional<UUID> getDiskId(ItemStack stack);

    Optional<StorageInfo> getInfo(Level level, ItemStack stack);

    boolean hasStacking();
}

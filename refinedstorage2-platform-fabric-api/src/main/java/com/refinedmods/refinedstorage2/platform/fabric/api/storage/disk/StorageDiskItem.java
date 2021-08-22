package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskInfo;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface StorageDiskItem {
    Optional<StorageChannelType<?>> getType(ItemStack stack);

    Optional<UUID> getDiskId(ItemStack stack);

    Optional<StorageDiskInfo> getInfo(@Nullable World world, ItemStack stack);
}

package com.refinedmods.refinedstorage2.platform.fabric.api.storage.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface StorageDiskItem {
    Optional<StorageChannelType<?>> getType(ItemStack stack);

    Optional<UUID> getDiskId(ItemStack stack);

    Optional<StorageInfo> getInfo(@Nullable World world, ItemStack stack);
}

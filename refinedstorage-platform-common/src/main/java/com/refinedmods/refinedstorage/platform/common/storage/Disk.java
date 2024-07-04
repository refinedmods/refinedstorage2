package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.storage.StorageState;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

public record Disk(@Nullable Item item, StorageState state) {
}

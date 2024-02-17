package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageState;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

public record Disk(@Nullable Item item, StorageState state) {
}

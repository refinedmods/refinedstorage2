package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageStorageState;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

public record DiskDriveDisk(@Nullable Item item, MultiStorageStorageState state) {
}

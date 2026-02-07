package com.refinedmods.refinedstorage.common.storage;

import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public interface StorageVariant {
    @Nullable
    Long getCapacity();

    @Nullable
    Item getStoragePart();

    String getName();
}

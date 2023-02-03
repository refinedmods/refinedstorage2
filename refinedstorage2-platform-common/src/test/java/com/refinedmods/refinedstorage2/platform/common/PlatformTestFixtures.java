package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.registry.PlatformRegistryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final PlatformRegistry<StorageType<?>> STORAGE_TYPE_REGISTRY = new PlatformRegistryImpl<>(
        new ResourceLocation("item"),
        ItemStorageType.INSTANCE
    );

    private PlatformTestFixtures() {
    }
}

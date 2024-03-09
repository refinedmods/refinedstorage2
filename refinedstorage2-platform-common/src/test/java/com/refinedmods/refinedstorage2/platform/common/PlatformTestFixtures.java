package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage2.platform.common.support.registry.PlatformRegistryImpl;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final PlatformRegistry<StorageType> STORAGE_TYPE_REGISTRY = new PlatformRegistryImpl<>(
        new ResourceLocation("item"),
        StorageTypes.ITEM
    );

    private PlatformTestFixtures() {
    }
}

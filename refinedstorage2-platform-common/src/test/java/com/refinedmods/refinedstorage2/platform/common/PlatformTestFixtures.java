package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistryImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final OrderedRegistry<ResourceLocation, StorageType<?>> STORAGE_TYPE_REGISTRY =
        new OrderedRegistryImpl<>(new ResourceLocation("item"), ItemStorageType.INSTANCE);


    private PlatformTestFixtures() {
    }
}

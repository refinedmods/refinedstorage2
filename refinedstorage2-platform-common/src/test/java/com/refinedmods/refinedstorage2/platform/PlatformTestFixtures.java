package com.refinedmods.refinedstorage2.platform;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceTypeRegistryImpl;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.StorageTypeRegistryImpl;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final ResourceTypeRegistry RESOURCE_TYPE_REGISTRY = new ResourceTypeRegistryImpl(ItemResourceType.INSTANCE);
    public static final StorageTypeRegistry STORAGE_TYPE_REGISTRY = new StorageTypeRegistryImpl();

    static {
        RESOURCE_TYPE_REGISTRY.register(FluidResourceType.INSTANCE);
        STORAGE_TYPE_REGISTRY.addType(new ResourceLocation("item"), ItemStorageType.INSTANCE);
    }

    private PlatformTestFixtures() {
    }
}

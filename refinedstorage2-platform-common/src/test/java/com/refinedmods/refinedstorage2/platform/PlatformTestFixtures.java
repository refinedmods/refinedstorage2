package com.refinedmods.refinedstorage2.platform;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistryImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.StorageTypeRegistryImpl;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final OrderedRegistry<ResourceLocation, ResourceType<?>> RESOURCE_TYPE_REGISTRY = new OrderedRegistryImpl<>(new ResourceLocation("item"), ItemResourceType.INSTANCE);
    public static final StorageTypeRegistry STORAGE_TYPE_REGISTRY = new StorageTypeRegistryImpl();

    static {
        RESOURCE_TYPE_REGISTRY.register(new ResourceLocation("fluid"), FluidResourceType.INSTANCE);
        STORAGE_TYPE_REGISTRY.addType(new ResourceLocation("item"), ItemStorageType.INSTANCE);
    }

    private PlatformTestFixtures() {
    }
}

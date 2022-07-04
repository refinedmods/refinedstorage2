package com.refinedmods.refinedstorage2.platform;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistryImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;

import net.minecraft.resources.ResourceLocation;

public final class PlatformTestFixtures {
    public static final OrderedRegistry<ResourceLocation, ResourceType> RESOURCE_TYPE_REGISTRY =
        new OrderedRegistryImpl<>(new ResourceLocation("item"), ItemResourceType.INSTANCE);
    public static final OrderedRegistry<ResourceLocation, StorageType<?>> STORAGE_TYPE_REGISTRY =
        new OrderedRegistryImpl<>(new ResourceLocation("item"), ItemStorageType.INSTANCE);

    static {
        RESOURCE_TYPE_REGISTRY.register(new ResourceLocation("fluid"), FluidResourceType.INSTANCE);
    }

    private PlatformTestFixtures() {
    }
}

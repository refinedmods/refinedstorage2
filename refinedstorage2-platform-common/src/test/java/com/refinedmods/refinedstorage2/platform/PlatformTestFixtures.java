package com.refinedmods.refinedstorage2.platform;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceTypeRegistryImpl;

public final class PlatformTestFixtures {
    public static final ResourceTypeRegistry RESOURCE_TYPE_REGISTRY = new ResourceTypeRegistryImpl(ItemResourceType.INSTANCE);

    static {
        RESOURCE_TYPE_REGISTRY.register(FluidResourceType.INSTANCE);
    }

    private PlatformTestFixtures() {
    }
}

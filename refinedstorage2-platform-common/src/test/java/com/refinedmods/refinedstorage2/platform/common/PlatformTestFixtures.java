package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;

public final class PlatformTestFixtures {
    public static final ResourceTypeRegistry RESOURCE_TYPE_REGISTRY = new ResourceTypeRegistry(ItemResourceType.INSTANCE);

    static {
        RESOURCE_TYPE_REGISTRY.register(FluidResourceType.INSTANCE);
    }

    private PlatformTestFixtures() {
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.containermenu;

import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceType;

public interface ResourceTypeAccessor {
    ResourceType<?> getCurrentResourceType();

    void toggleResourceType();
}

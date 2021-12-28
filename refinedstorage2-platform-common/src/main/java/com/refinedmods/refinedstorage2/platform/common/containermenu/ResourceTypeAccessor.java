package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

public interface ResourceTypeAccessor {
    ResourceType<?> getCurrentResourceType();

    void toggleResourceType();
}

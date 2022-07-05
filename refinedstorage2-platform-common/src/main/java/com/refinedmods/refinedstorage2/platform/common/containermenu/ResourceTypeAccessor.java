package com.refinedmods.refinedstorage2.platform.common.containermenu;

import net.minecraft.network.chat.Component;

public interface ResourceTypeAccessor {
    Component getCurrentResourceTypeName();

    void toggleResourceType();
}

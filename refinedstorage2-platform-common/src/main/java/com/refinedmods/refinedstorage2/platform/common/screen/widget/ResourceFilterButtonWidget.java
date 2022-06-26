package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceTypeAccessor;

import net.minecraft.client.gui.components.Button;

public class ResourceFilterButtonWidget extends Button {
    public static final int WIDTH = 50;

    public ResourceFilterButtonWidget(int x, int y, ResourceTypeAccessor resourceTypeAccessor) {
        super(x, y, WIDTH, 15, resourceTypeAccessor.getCurrentResourceTypeName(), createPressAction(resourceTypeAccessor));
    }

    private static OnPress createPressAction(ResourceTypeAccessor resourceTypeAccessor) {
        return btn -> {
            resourceTypeAccessor.toggleResourceType();
            btn.setMessage(resourceTypeAccessor.getCurrentResourceTypeName());
        };
    }
}

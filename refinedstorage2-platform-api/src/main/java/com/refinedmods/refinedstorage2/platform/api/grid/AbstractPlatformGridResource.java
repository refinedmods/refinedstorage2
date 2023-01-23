package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public abstract class AbstractPlatformGridResource extends AbstractGridResource {
    protected AbstractPlatformGridResource(
        final ResourceAmount<?> resourceAmount,
        final String name,
        final Map<GridResourceAttributeKey, Set<String>> attributes
    ) {
        super(resourceAmount, name, attributes);
    }

    public abstract void render(PoseStack poseStack, int slotX, int slotY);

    public abstract String getDisplayedAmount();

    public abstract String getAmountInTooltip();

    public abstract List<Component> getTooltip();
}

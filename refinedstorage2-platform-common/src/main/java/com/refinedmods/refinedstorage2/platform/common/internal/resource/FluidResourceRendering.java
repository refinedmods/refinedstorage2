package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FluidResourceRendering implements ResourceRendering<FluidResource> {
    @Override
    public String getDisplayedAmount(final long amount) {
        return Platform.INSTANCE.getBucketAmountFormatter().formatWithUnits(amount);
    }

    @Override
    public Component getDisplayName(final FluidResource resource) {
        return Platform.INSTANCE.getFluidRenderer().getDisplayName(resource);
    }

    @Override
    public List<Component> getTooltip(final FluidResource resource) {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(resource);
    }

    @Override
    public void render(final FluidResource resource, final GuiGraphics graphics, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, resource);
    }
}

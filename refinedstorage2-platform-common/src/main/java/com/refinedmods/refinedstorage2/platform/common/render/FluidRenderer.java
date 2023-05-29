package com.refinedmods.refinedstorage2.platform.common.render;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public interface FluidRenderer {
    void render(PoseStack poseStack, int x, int y, FluidResource fluidResource);

    List<Component> getTooltip(FluidResource fluidResource);
}

package com.refinedmods.refinedstorage2.platform.common.support.render;

import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public interface FluidRenderer {
    void render(PoseStack poseStack, int x, int y, FluidResource fluidResource);

    void render(PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, FluidResource fluidResource);

    List<Component> getTooltip(FluidResource fluidResource);

    Component getDisplayName(FluidResource fluidResource);
}

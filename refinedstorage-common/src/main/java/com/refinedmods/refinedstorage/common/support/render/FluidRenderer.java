package com.refinedmods.refinedstorage.common.support.render;

import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;

public interface FluidRenderer {
    void render(GuiGraphicsExtractor graphics, int x, int y, FluidResource resource);

    void render(PoseStack poseStack, SubmitNodeCollector nodes, int light, long seed, FluidResource resource);

    List<Component> getTooltip(FluidResource resource);

    Component getDisplayName(FluidResource resource);
}

package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public interface FluidRenderer {
    void render(PoseStack poseStack, int x, int y, int z, FluidResource fluidResource);

    List<Component> getTooltip(FluidResource fluidResource);
}

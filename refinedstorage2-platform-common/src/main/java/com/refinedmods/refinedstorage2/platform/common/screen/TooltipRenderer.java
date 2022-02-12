package com.refinedmods.refinedstorage2.platform.common.screen;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface TooltipRenderer {
    void render(PoseStack poseStack, List<Component> lines, int x, int y);
}

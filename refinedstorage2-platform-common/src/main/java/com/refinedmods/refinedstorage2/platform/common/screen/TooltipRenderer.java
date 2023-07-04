package com.refinedmods.refinedstorage2.platform.common.screen;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface TooltipRenderer {
    void render(GuiGraphics graphics, List<Component> lines, int x, int y);
}

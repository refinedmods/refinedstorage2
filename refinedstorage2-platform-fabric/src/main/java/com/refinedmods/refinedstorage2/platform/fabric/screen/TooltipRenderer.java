package com.refinedmods.refinedstorage2.platform.fabric.screen;

import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@FunctionalInterface
public interface TooltipRenderer {
    void render(MatrixStack matrices, List<Text> lines, int x, int y);
}

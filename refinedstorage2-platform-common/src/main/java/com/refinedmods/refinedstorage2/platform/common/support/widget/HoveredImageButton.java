package com.refinedmods.refinedstorage2.platform.common.support.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HoveredImageButton extends ImageButton {
    public HoveredImageButton(final int x,
                              final int y,
                              final int width,
                              final int height,
                              final WidgetSprites sprites,
                              final OnPress onPress,
                              final Component component) {
        super(x, y, width, height, sprites, onPress, component);
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int x, final int y, final float partialTicks) {
        // only takes isHovered in account, not isFocused
        final ResourceLocation location = sprites.get(isActive(), isHovered());
        graphics.blitSprite(location, getX(), getY(), width, height);
    }
}

package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageButton extends Button {
    public static final int BUTTON_SIZE = 16;
    private static final int ICON_SIZE = 12;

    private ResourceLocation sprite;

    public ImageButton(final int x,
                       final int y,
                       final ResourceLocation sprite,
                       final Consumer<ImageButton> onPress) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.empty(), button -> onPress.accept((ImageButton) button),
            DEFAULT_NARRATION);
        this.sprite = sprite;
    }

    public void setSprite(final ResourceLocation sprite) {
        this.sprite = sprite;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int x, final int y, final float partialTicks) {
        super.renderWidget(graphics, x, y, partialTicks);
        graphics.blitSprite(sprite, getX() + 2, getY() + 2, ICON_SIZE, ICON_SIZE);
    }
}

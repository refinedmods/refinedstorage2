package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class ImageButton extends Button {
    public static final int BUTTON_SIZE = 16;
    private static final int ICON_SIZE = 12;

    private Identifier sprite;

    public ImageButton(final int x, final int y, final Identifier sprite, final Consumer<ImageButton> onPress) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.empty(), button -> onPress.accept((ImageButton) button),
            DEFAULT_NARRATION);
        this.sprite = sprite;
    }

    public void setSprite(final Identifier sprite) {
        this.sprite = sprite;
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        extractDefaultSprite(graphics);
        graphics.blitSprite(GUI_TEXTURED, sprite, getX() + 2, getY() + 2, ICON_SIZE, ICON_SIZE);
    }
}

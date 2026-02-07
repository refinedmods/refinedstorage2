package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class CustomButton extends Button {
    protected WidgetSprites sprites;

    public CustomButton(final int x,
                        final int y,
                        final int width,
                        final int height,
                        final WidgetSprites sprites,
                        final Consumer<CustomButton> onPress,
                        final Component component) {
        super(x, y, width, height, component, button -> onPress.accept((CustomButton) button), DEFAULT_NARRATION);
        this.sprites = sprites;
    }

    public void setSprites(final WidgetSprites sprites) {
        this.sprites = sprites;
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        // only takes isHovered in account, not isFocused
        final Identifier location = sprites.get(isActive(), isHovered());
        graphics.blitSprite(GUI_TEXTURED, location, getX(), getY(), width, height);
    }
}

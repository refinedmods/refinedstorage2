package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class SearchIconWidget extends AbstractWidget {
    public static final Identifier SEARCH = createIdentifier("search");
    public static final int SEARCH_SIZE = 12;

    private final Supplier<Component> messageSupplier;
    private final EditBox editBox;

    public SearchIconWidget(final int x,
                            final int y,
                            final Supplier<Component> messageSupplier,
                            final EditBox editBox) {
        super(x, y, SEARCH_SIZE, SEARCH_SIZE, Component.empty());
        this.messageSupplier = messageSupplier;
        this.editBox = editBox;
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            editBox.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics,
                                            final int mouseX,
                                            final int mouseY,
                                            final float partialTicks) {
        graphics.blitSprite(GUI_TEXTURED, SEARCH, getX(), getY(), SEARCH_SIZE, SEARCH_SIZE);
        if (isHovered) {
            setTooltip(Tooltip.create(messageSupplier.get()));
        } else {
            setTooltip(null);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }
}

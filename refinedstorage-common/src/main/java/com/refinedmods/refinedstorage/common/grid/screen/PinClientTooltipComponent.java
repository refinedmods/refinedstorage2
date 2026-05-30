package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class PinClientTooltipComponent implements ClientTooltipComponent {
    public static final PinClientTooltipComponent INSTANCE = new PinClientTooltipComponent();

    private static final Identifier ICON = createIdentifier("grid/pin");
    private static final int ICON_SIZE = 16;
    private static final int ICON_MARGIN = 4;

    private static final Component DRAG_AND_DROP_TO_PIN = createTranslation("gui", "grid.drag_and_drop_to_pin");

    private PinClientTooltipComponent() {
    }

    @Override
    public int getHeight(final Font font) {
        return ICON_SIZE;
    }

    @Override
    public int getWidth(final Font font) {
        return ICON_SIZE + ICON_MARGIN + (int) (font.width(DRAG_AND_DROP_TO_PIN) * SmallText.TOOLTIP_SCALE);
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        final int yOffset = SmallText.isSmall() ? 5 : 0;
        SmallText.render(
            graphics,
            font,
            DRAG_AND_DROP_TO_PIN.getVisualOrderText(),
            x + ICON_SIZE + ICON_MARGIN - 4,
            y + yOffset,
            0xFFEF2E39,
            true,
            SmallText.TOOLTIP_SCALE
        );
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        graphics.blitSprite(GUI_TEXTURED, ICON, x - 2, y, ICON_SIZE, ICON_SIZE);
    }
}

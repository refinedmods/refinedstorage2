package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class AutocraftableClientTooltipComponent implements ClientTooltipComponent {
    private static final Identifier ICON = createIdentifier("grid/craftable");
    private static final int ICON_SIZE = 9;
    private static final int ICON_MARGIN = 4;

    private static final Component AUTOCRAFTABLE = createTranslation("gui", "grid.autocraftable");
    private static final Component PATTERN_IN_INVENTORY = createTranslation("gui", "grid.pattern_in_inventory");
    private static final Component EMPTY = createTranslation("gui", "grid.click_to_autocraft");
    private static final Component EXISTING = createTranslation("gui", "grid.ctrl_click_to_autocraft");

    private final Component text;

    private AutocraftableClientTooltipComponent(final Component text) {
        this.text = text;
    }

    static AutocraftableClientTooltipComponent autocraftable(final AutocraftableResourceHint hint) {
        return new AutocraftableClientTooltipComponent(hint == AutocraftableResourceHint.AUTOCRAFTABLE
            ? AUTOCRAFTABLE
            : PATTERN_IN_INVENTORY);
    }

    static AutocraftableClientTooltipComponent empty() {
        return new AutocraftableClientTooltipComponent(EMPTY);
    }

    static AutocraftableClientTooltipComponent existing() {
        return new AutocraftableClientTooltipComponent(EXISTING);
    }

    @Override
    public int getHeight(final Font font) {
        return ICON_SIZE + 2;
    }

    @Override
    public int getWidth(final Font font) {
        return ICON_SIZE + ICON_MARGIN + (int) (font.width(text) * SmallText.TOOLTIP_SCALE);
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        final int yOffset = SmallText.isSmall() ? 2 : 0;
        SmallText.render(
            graphics,
            font,
            text.getVisualOrderText(),
            x + ICON_SIZE + ICON_MARGIN,
            y + yOffset,
            0xFF9F7F50,
            true,
            SmallText.TOOLTIP_SCALE
        );
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        graphics.blitSprite(GUI_TEXTURED, ICON, x, y, ICON_SIZE, ICON_SIZE);
    }
}

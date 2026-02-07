package com.refinedmods.refinedstorage.common.support.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class HelpClientTooltipComponent implements ClientTooltipComponent {
    private static final Identifier SPRITE = createIdentifier("help");
    private static final ClientTooltipComponent PRESS_SHIFT_FOR_HELP = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("misc", "press_shift_for_help")
    );
    private static final Style STYLE = Style.EMPTY.withColor(0xFF129ED9);
    private static final int MAX_CHARS = 200;
    private static final int HELP_ICON_SIZE = 20;
    private static final int HELP_ICON_MARGIN = 4;

    private final List<FormattedCharSequence> lines;
    private final int paddingTop;

    private HelpClientTooltipComponent(final Component text, final int paddingTop) {
        this.lines = Language.getInstance().getVisualOrder(
            Minecraft.getInstance().font.getSplitter().splitLines(text, MAX_CHARS, STYLE)
        );
        this.paddingTop = paddingTop;
    }

    @Override
    public int getHeight(final Font font) {
        return Math.max(HELP_ICON_SIZE + paddingTop, (9 * lines.size()) + paddingTop);
    }

    @Override
    public int getWidth(final Font font) {
        int width = 0;
        for (final FormattedCharSequence line : lines) {
            final float scale = SmallText.correctScale(SmallText.TOOLTIP_SCALE);
            final int lineWidth = HELP_ICON_SIZE + HELP_ICON_MARGIN + (int) (font.width(line) * scale);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        return width;
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        final int xx = x + HELP_ICON_SIZE + HELP_ICON_MARGIN;
        int yy = y + paddingTop;
        for (final FormattedCharSequence line : lines) {
            SmallText.render(graphics, font, line, xx, yy, -1, true, SmallText.TOOLTIP_SCALE);
            yy += 9;
        }
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        graphics.blitSprite(GUI_TEXTURED, SPRITE, x, y + (paddingTop / 2), HELP_ICON_SIZE, HELP_ICON_SIZE);
    }

    public static ClientTooltipComponent create(final Component text) {
        if (Minecraft.getInstance().hasShiftDown()) {
            return new HelpClientTooltipComponent(text, SmallText.isSmall() ? 4 : 0);
        } else {
            return PRESS_SHIFT_FOR_HELP;
        }
    }

    public static ClientTooltipComponent createAlwaysDisplayed(final Component text) {
        return new HelpClientTooltipComponent(text, 0);
    }
}

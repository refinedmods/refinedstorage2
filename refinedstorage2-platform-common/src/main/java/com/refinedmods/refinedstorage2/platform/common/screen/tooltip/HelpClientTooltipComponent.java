package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationAsHeading;
import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

public class HelpClientTooltipComponent implements ClientTooltipComponent {
    private static final ClientTooltipComponent PRESS_SHIFT_FOR_HELP = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("misc", "press_shift_for_help")
    );
    private static final Style STYLE = Style.EMPTY.withColor(0xFF129ED9);
    private static final int MAX_CHARS = 200;

    private final List<FormattedCharSequence> lines;
    private final float scale;

    private HelpClientTooltipComponent(final Component text) {
        this.lines = Language.getInstance().getVisualOrder(
            Minecraft.getInstance().font.getSplitter().splitLines(text, MAX_CHARS, STYLE)
        );
        this.scale = SmallText.getScale();
    }

    @Override
    public int getHeight() {
        return Math.max(20 + 4, (9 * lines.size()) + 4);
    }

    @Override
    public int getWidth(final Font font) {
        int width = 0;
        for (final FormattedCharSequence line : lines) {
            final int lineWidth = 20 + 4 + (int) (font.width(line) * scale);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        return width;
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f pose,
                           final MultiBufferSource.BufferSource buffer) {
        final int xx = x + 20 + 4;
        int yy = y + 4;
        for (final FormattedCharSequence line : lines) {
            SmallText.render(
                font,
                line,
                xx,
                yy,
                scale,
                pose,
                buffer
            );
            yy += 9;
        }
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.blit(TextureIds.ICONS, x, y + 2, 236, 158, 20, 20);
    }

    public static ClientTooltipComponent create(final Component text) {
        if (hasShiftDown()) {
            return new HelpClientTooltipComponent(text);
        } else {
            return PRESS_SHIFT_FOR_HELP;
        }
    }
}

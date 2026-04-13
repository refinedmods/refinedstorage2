package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class AutocraftableClientTooltipComponent implements ClientTooltipComponent {
    private static final ResourceLocation ICON = createIdentifier("grid/craftable");
    private static final int ICON_SIZE = 9;
    private static final int ICON_MARGIN = 4;

    private static final Component AUTOCRAFTABLE = createTranslation("gui", "grid.autocraftable");
    private static final Component PATTERN_IN_INVENTORY = createTranslation("gui", "grid.pattern_in_inventory");
    private static final Component EMPTY = createTranslation("gui", "grid.click_to_autocraft");
    private static final Component EXISTING = createTranslation("gui", "grid.ctrl_click_to_autocraft");
    private static final Component CMD_EXISTING = createTranslation("gui", "grid.cmd_click_to_autocraft");

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
        return new AutocraftableClientTooltipComponent(ClientPlatformUtil.isCommand() ? CMD_EXISTING : EXISTING);
    }

    @Override
    public int getHeight() {
        return ICON_SIZE + 2;
    }

    @Override
    public int getWidth(final Font font) {
        return ICON_SIZE + ICON_MARGIN + (int) (font.width(text) * SmallText.TOOLTIP_SCALE);
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f matrix,
                           final MultiBufferSource.BufferSource bufferSource) {
        final int yOffset = SmallText.isSmall() ? 2 : 0;
        SmallText.render(
            font,
            text.getVisualOrderText(),
            x + ICON_SIZE + ICON_MARGIN,
            y + yOffset,
            0x9F7F50,
            matrix,
            bufferSource,
            SmallText.TOOLTIP_SCALE
        );
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.blitSprite(ICON, x, y, ICON_SIZE, ICON_SIZE);
    }
}

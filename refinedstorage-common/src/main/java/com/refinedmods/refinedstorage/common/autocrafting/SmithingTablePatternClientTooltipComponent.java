package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_WIDTH;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class SmithingTablePatternClientTooltipComponent implements ClientTooltipComponent {
    private static final int ARROW_SPACING = 8;

    private final Component outputText;
    private final PatternResolver.ResolvedSmithingTablePattern pattern;

    SmithingTablePatternClientTooltipComponent(final PatternResolver.ResolvedSmithingTablePattern pattern) {
        this.outputText = getOutputText(pattern.output());
        this.pattern = pattern;
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        graphics.text(font, outputText, x, y, 0xFFAAAAAA);
        final int slotsY = y + 9 + 2;
        graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, x, slotsY, 18, 18);
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(ItemResource.class);
        rendering.render(pattern.template(), graphics, x + 1, slotsY + 1);
        graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, x + 18, slotsY, 18, 18);
        rendering.render(pattern.base(), graphics, x + 18 + 1, slotsY + 1);
        graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, x + 18 + 18, slotsY, 18, 18);
        rendering.render(pattern.addition(), graphics, x + 18 + 18 + 1, slotsY + 1);
        graphics.blitSprite(GUI_TEXTURED,
            LIGHT_ARROW,
            x + (18 * 3) + ARROW_SPACING,
            y + 9 + 2 + (18 / 2) - (LIGHT_ARROW_HEIGHT / 2),
            LIGHT_ARROW_WIDTH,
            LIGHT_ARROW_HEIGHT
        );
        final int lastSlotX = x + (18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING;
        graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, lastSlotX, slotsY, 18, 18);
        rendering.render(pattern.output(), graphics, lastSlotX + 1, slotsY + 1);
    }

    @Override
    public int getHeight(final Font font) {
        return 9 + 2 + 18 + 3;
    }

    @Override
    public int getWidth(final Font font) {
        return Math.max(
            font.width(outputText),
            (18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING + 18
        );
    }

    private static Component getOutputText(final ItemResource output) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(ItemResource.class);
        return Component.literal("1x ")
            .append(rendering.getDisplayName(output))
            .withStyle(ChatFormatting.GRAY);
    }
}

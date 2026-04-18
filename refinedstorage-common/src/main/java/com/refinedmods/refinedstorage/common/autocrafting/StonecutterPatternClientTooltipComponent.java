package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_WIDTH;
import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;

class StonecutterPatternClientTooltipComponent implements ClientTooltipComponent {
    private static final int ARROW_SPACING = 8;

    private final Component outputText;
    private final ItemResource input;
    private final ResourceAmount output;

    StonecutterPatternClientTooltipComponent(final PatternResolver.ResolvedStonecutterPattern pattern) {
        this.outputText = getOutputText(pattern.output());
        this.input = pattern.input();
        this.output = pattern.output();
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.drawString(font, outputText, x, y, 0xAAAAAA);
        final int slotY = y + 9 + 2;
        graphics.blitSprite(SLOT, x, y + 9 + 2, 18, 18);
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(ItemResource.class);
        rendering.render(input, graphics, x + 1, y + 9 + 2 + 1);
        graphics.blitSprite(
            LIGHT_ARROW,
            x + 18 + ARROW_SPACING,
            y + 9 + 2 + (18 / 2) - (LIGHT_ARROW_HEIGHT / 2),
            LIGHT_ARROW_WIDTH,
            LIGHT_ARROW_HEIGHT
        );
        final int slotX = x + 18 + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING;
        graphics.blitSprite(
            VanillaConstants.STONECUTTER_RECIPE_SELECTED_SPRITE,
            slotX,
            slotY,
            16,
            18
        );
        rendering.render(
            output.resource(),
            graphics,
            slotX,
            slotY + 1
        );
        ResourceSlotRendering.renderAmount(graphics, slotX - 1, slotY + 1, output.amount(), rendering);
    }

    private static Component getOutputText(final ResourceAmount resourceAmount) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            resourceAmount.resource().getClass()
        );
        final String displayAmount = rendering.formatAmount(resourceAmount.amount());
        return Component.literal(String.format("%sx ", displayAmount))
            .append(rendering.getDisplayName(resourceAmount.resource()))
            .withStyle(ChatFormatting.GRAY);
    }

    @Override
    public int getHeight() {
        return 9 + 2 + 18 + 3;
    }

    @Override
    public int getWidth(final Font font) {
        return Math.max(
            font.width(outputText),
            18 + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING + 16
        );
    }
}

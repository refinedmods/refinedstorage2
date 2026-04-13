package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_WIDTH;
import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class CraftingPatternClientTooltipComponent implements ClientTooltipComponent {
    private static final long CYCLE_MS = 1000;
    private static final int ARROW_SPACING = 8;
    private static final Identifier LARGE_SLOT = createIdentifier("large_slot");
    private static final int LARGE_SLOT_WIDTH = 26;
    private static final int LARGE_SLOT_HEIGHT = 26;

    private final int width;
    private final int height;
    private final PatternResolver.ResolvedCraftingPattern pattern;

    @Nullable
    private final ItemStack outputStack;
    @Nullable
    private final Component outputText;

    private long cycleStart = 0;
    private int currentCycle = 0;

    CraftingPatternClientTooltipComponent(final int width,
                                          final int height,
                                          final PatternResolver.ResolvedCraftingPattern pattern) {
        this.width = width;
        this.height = height;
        this.pattern = pattern;
        final ItemResource outputResource = pattern.output().resource() instanceof ItemResource itemResource
            ? itemResource
            : null;
        this.outputStack = outputResource != null
            ? outputResource.toItemStack(pattern.output().amount())
            : null;
        this.outputText = outputResource != null
            ? Component.literal(String.format("%dx ", pattern.output().amount()))
              .append(outputResource.toItemStack().getHoverName())
              .withStyle(ChatFormatting.GRAY) : null;
    }

    @Override
    public int getHeight(final Font font) {
        return 9 + 2 + height * 18 + 3;
    }

    @Override
    public int getWidth(final Font font) {
        return Math.max(
            outputText != null ? font.width(outputText) : 0,
            (width * 18) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING + LARGE_SLOT_WIDTH
        );
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        final long now = System.currentTimeMillis();
        if (cycleStart == 0) {
            cycleStart = now;
        }
        if (now - cycleStart >= CYCLE_MS) {
            currentCycle++;
            cycleStart = now;
        }
        if (outputText != null) {
            graphics.text(font, outputText, x, y, 0xFFAAAAAA);
        }
        renderInputSlots(x, y + 9 + 2, graphics);
        renderArrow(x, y + 9 + 2, graphics);
        renderResultSlot(font, x, y + 9 + 2, graphics);
    }

    private void renderInputSlots(final int x, final int y, final GuiGraphicsExtractor graphics) {
        for (int sx = 0; sx < width; ++sx) {
            for (int sy = 0; sy < height; ++sy) {
                renderInputSlot(x, y, graphics, sx, sy);
            }
        }
    }

    private void renderInputSlot(final int x, final int y, final GuiGraphicsExtractor graphics, final int sx,
                                 final int sy) {
        graphics.blitSprite(GUI_TEXTURED, SLOT, x + sx * 18, y + sy * 18, 18, 18);
        final int index = sy * width + sx;
        final List<ResourceKey> inputs = pattern.inputs().get(index);
        if (inputs.isEmpty()) {
            return;
        }
        final int idx = currentCycle % inputs.size();
        final ResourceKey resource = inputs.get(idx);
        RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass()).render(
            resource,
            graphics,
            x + sx * 18 + 1,
            y + sy * 18 + 1
        );
    }

    private void renderArrow(final int x, final int y, final GuiGraphicsExtractor graphics) {
        graphics.blitSprite(
            GUI_TEXTURED,
            LIGHT_ARROW,
            x + width * 18 + ARROW_SPACING,
            y + ((height * 18) / 2) - (LIGHT_ARROW_HEIGHT / 2),
            LIGHT_ARROW_WIDTH,
            LIGHT_ARROW_HEIGHT
        );
    }

    private void renderResultSlot(final Font font, final int x, final int y, final GuiGraphicsExtractor graphics) {
        final int slotX = x + width * 18 + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING;
        final int slotY = y + ((height * 18) / 2) - (LARGE_SLOT_HEIGHT / 2);
        graphics.blitSprite(GUI_TEXTURED, LARGE_SLOT, slotX, slotY, LARGE_SLOT_WIDTH, LARGE_SLOT_HEIGHT);
        if (outputStack != null) {
            final int stackX = slotX + 5;
            final int stackY = slotY + 5;
            graphics.item(outputStack, stackX, stackY);
            graphics.itemDecorations(font, outputStack, stackX, stackY);
        }
    }
}

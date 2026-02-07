package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_WIDTH;
import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class ProcessingPatternClientTooltipComponent implements ClientTooltipComponent {
    private static final long CYCLE_MS = 1000;
    private static final int ARROW_SPACING = 8;

    private final int rows;
    private final List<Component> outputTexts;
    private final List<List<ResourceAmount>> inputs;
    private final List<List<ResourceAmount>> outputs;

    private long cycleStart = 0;
    private int currentCycle = 0;

    ProcessingPatternClientTooltipComponent(final ProcessingPatternState state) {
        this.rows = calculateMaxRows(state);
        this.outputTexts = getOutputText(state);
        this.inputs = state.ingredients()
            .stream()
            .map(processingIngredient ->
                processingIngredient
                    .stream()
                    .flatMap(i -> i.calculateInputsIncludingAlternatives().stream()
                        .map(resource -> new ResourceAmount(resource, i.input().amount())))
                    .toList()
            ).toList();
        this.outputs = state.outputs().stream().map(output -> output.map(List::of).orElse(List.of())).toList();
    }

    private static int calculateMaxRows(final ProcessingPatternState state) {
        int lastFilledInputIndex = 0;
        for (int i = 0; i < state.ingredients().size(); i++) {
            if (state.ingredients().get(i).isPresent()) {
                lastFilledInputIndex = i;
            }
        }
        int lastFilledOutputIndex = 0;
        for (int i = 0; i < state.outputs().size(); i++) {
            if (state.outputs().get(i).isPresent()) {
                lastFilledOutputIndex = i;
            }
        }
        final int lastFilledInputRow = Math.ceilDiv(lastFilledInputIndex + 1, 3);
        final int lastFilledOutputRow = Math.ceilDiv(lastFilledOutputIndex + 1, 3);
        return Math.max(lastFilledInputRow, lastFilledOutputRow);
    }

    private static List<Component> getOutputText(final ProcessingPatternState state) {
        return state.getFlatOutputs()
            .stream()
            .map(ProcessingPatternClientTooltipComponent::getOutputText)
            .toList();
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
    public int getHeight(final Font font) {
        return (outputTexts.size() * font.lineHeight) + 2 + (rows * 18) + 3;
    }

    @Override
    public int getWidth(final Font font) {
        return Math.max(
            outputTexts.stream().mapToInt(font::width).max().orElse(0),
            (18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING + (18 * 3)
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
        renderOutputText(font, x, y, graphics);
        final int matrixSlotsY = y + (outputTexts.size() * 9) + 2;
        renderMatrixSlots(x, matrixSlotsY, true, graphics);
        graphics.blitSprite(
            GUI_TEXTURED,
            LIGHT_ARROW,
            x + (18 * 3) + ARROW_SPACING,
            y + (outputTexts.size() * 9) + 2 + ((rows * 18) / 2) - (LIGHT_ARROW_HEIGHT / 2),
            LIGHT_ARROW_WIDTH,
            LIGHT_ARROW_HEIGHT
        );
        renderMatrixSlots(x, matrixSlotsY, false, graphics);
    }

    private void renderMatrixSlots(final int x,
                                   final int y,
                                   final boolean input,
                                   final GuiGraphicsExtractor graphics) {
        final int maxSize = input ? inputs.size() : outputs.size();
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < 3; ++column) {
                final int slotXOffset = !input ? ((18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING) : 0;
                final int slotX = x + slotXOffset + column * 18;
                final int slotY = y + row * 18;
                final int idx = row * 3 + column;
                if (idx >= maxSize) {
                    break;
                }
                if (input) {
                    renderMatrixSlot(graphics, slotX, slotY, inputs.get(idx));
                } else {
                    renderMatrixSlot(graphics, slotX, slotY, outputs.get(idx));
                }
            }
        }
    }

    private void renderMatrixSlot(
        final GuiGraphicsExtractor graphics,
        final int slotX,
        final int slotY,
        final List<ResourceAmount> possibilities
    ) {
        graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, 18, 18);
        if (possibilities.isEmpty()) {
            return;
        }
        final ResourceAmount resourceAmount = possibilities.get(currentCycle % possibilities.size());
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            resourceAmount.resource().getClass()
        );
        rendering.render(resourceAmount.resource(), graphics, slotX + 1, slotY + 1);
        ResourceSlotRendering.renderAmount(graphics, slotX + 1, slotY + 1, resourceAmount.amount(), rendering);
    }

    private void renderOutputText(final Font font, final int x, final int y, final GuiGraphicsExtractor graphics) {
        for (int i = 0; i < outputTexts.size(); ++i) {
            graphics.text(
                font,
                outputTexts.get(i),
                x,
                y + (i * 9),
                0xFFAAAAAA
            );
        }
    }
}

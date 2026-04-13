package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.function.Consumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_PADDING;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_WIDTH;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class ProcessingPatternGridRenderer implements PatternGridRenderer {
    private static final Identifier PROCESSING = createIdentifier("pattern_grid/processing");
    private static final Identifier PROCESSING_MATRIX = createIdentifier("pattern_grid/processing_matrix");
    private static final int INDIVIDUAL_PROCESSING_MATRIX_SIZE = 54;
    private static final int PROCESSING_MATRIX_SLOT_SIZE = 18;
    private static final int PROCESSING_INSET_Y_PADDING = 9;
    private static final MutableComponent INPUTS = createTranslation("gui", "pattern_grid.processing.inputs");
    private static final MutableComponent OUTPUTS = createTranslation("gui", "pattern_grid.processing.outputs");

    private final PatternGridContainerMenu menu;
    private final int leftPos;
    private final int topPos;
    private final int x;
    private final int y;

    @Nullable
    private ScrollbarWidget scrollbar;

    ProcessingPatternGridRenderer(final PatternGridContainerMenu menu,
                                  final int leftPos,
                                  final int topPos,
                                  final int x,
                                  final int y) {
        this.menu = menu;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.x = x;
        this.y = y;
    }

    @Override
    public void addWidgets(final Consumer<AbstractWidget> widgets, final Consumer<AbstractWidget> renderables) {
        scrollbar = createProcessingScrollbar();
        updateScrollbarMaxOffset();
        widgets.accept(scrollbar);
    }

    private void updateScrollbarMaxOffset() {
        if (scrollbar == null) {
            return;
        }

        int filledInputSlots = 0;
        int filledOutputSlots = 0;
        int lastFilledInputSlot = 0;
        int lastFilledOutputSlot = 0;

        for (int i = 0; i < menu.getResourceSlots().size(); ++i) {
            final ResourceSlot resourceSlot = menu.getResourceSlots().get(i);
            if (resourceSlot.isEmpty() || !(resourceSlot instanceof ProcessingMatrixResourceSlot matrixSlot)) {
                continue;
            }
            if (matrixSlot.isInput()) {
                filledInputSlots++;
                lastFilledInputSlot = i;
            } else {
                filledOutputSlots++;
                lastFilledOutputSlot = i - 81;
            }
        }

        final int maxFilledSlots = Math.max(filledInputSlots, filledOutputSlots);
        final int maxFilledRows = Math.floorDiv(maxFilledSlots - 1, 3);

        final int maxLastFilledSlot = Math.max(lastFilledInputSlot, lastFilledOutputSlot);
        final int maxLastFilledRow = Math.floorDiv(maxLastFilledSlot, 3) - 2;

        final int maxOffset = Math.max(maxFilledRows, maxLastFilledRow);
        final int maxOffsetCorrected = scrollbar.isSmoothScrolling()
            ? maxOffset * PROCESSING_MATRIX_SLOT_SIZE
            : maxOffset;

        scrollbar.setMaxOffset(maxOffsetCorrected);
        scrollbar.setEnabled(maxOffsetCorrected > 0);
    }

    private ScrollbarWidget createProcessingScrollbar() {
        final ScrollbarWidget s = new ScrollbarWidget(
            x + 126,
            y + 14,
            ScrollbarWidget.Type.SMALL,
            52
        );
        s.visible = isScrollbarVisible(menu);
        s.setListener(offset -> onScrollbarChanged((int) offset));
        return s;
    }

    private void onScrollbarChanged(final int offset) {
        int inputRow = 0;
        int outputRow = 0;
        final int scrollbarOffset = (scrollbar != null && scrollbar.isSmoothScrolling())
            ? offset
            : offset * PROCESSING_MATRIX_SLOT_SIZE;
        for (int i = 0; i < menu.getResourceSlots().size(); ++i) {
            final ResourceSlot slot = menu.getResourceSlots().get(i);
            if (!(slot instanceof ProcessingMatrixResourceSlot matrixSlot)) {
                continue;
            }
            final int row = matrixSlot.isInput() ? inputRow : outputRow;
            final int slotY = y
                + INSET_PADDING
                + PROCESSING_INSET_Y_PADDING + 1
                + (row * PROCESSING_MATRIX_SLOT_SIZE)
                - scrollbarOffset
                - topPos;
            Platform.INSTANCE.setSlotY(menu.getResourceSlots().get(i), slotY);
            if ((i + 1) % 3 == 0) {
                if (matrixSlot.isInput()) {
                    inputRow++;
                } else {
                    outputRow++;
                }
            }
        }
    }

    private static boolean isScrollbarVisible(final PatternGridContainerMenu menu) {
        return menu.getPatternType() == PatternType.PROCESSING;
    }

    @Override
    public void tick() {
        updateScrollbarMaxOffset();
    }

    @Override
    public void render(final GuiGraphicsExtractor graphics,
                       final int mouseX,
                       final int mouseY,
                       final float partialTicks) {
        if (scrollbar != null) {
            scrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public int getClearButtonX() {
        return leftPos + 124;
    }

    @Override
    public int getClearButtonY() {
        return y + INSET_PADDING + PROCESSING_INSET_Y_PADDING;
    }

    @Override
    public void renderBackground(final GuiGraphicsExtractor graphics,
                                 final float partialTicks,
                                 final int mouseX,
                                 final int mouseY) {
        graphics.blitSprite(GUI_TEXTURED, PROCESSING,
            x + INSET_PADDING, y + INSET_PADDING + PROCESSING_INSET_Y_PADDING, 130, 54);
        renderMatrix(
            graphics,
            x + INSET_PADDING + 1,
            x + INSET_PADDING + 1 + 52 + 1, // include the edge so we get the item counts properly
            mouseX,
            mouseY,
            true
        );
        renderMatrix(
            graphics,
            x + INSET_PADDING + INDIVIDUAL_PROCESSING_MATRIX_SIZE + 2 + 1,
            x + INSET_PADDING + INDIVIDUAL_PROCESSING_MATRIX_SIZE + 2 + 1 + 52 + 1,
            mouseX,
            mouseY,
            false
        );
    }

    private void renderMatrix(final GuiGraphicsExtractor graphics,
                              final int startX,
                              final int endX,
                              final int mouseX,
                              final int mouseY,
                              final boolean input) {
        final int startY = y + 14;
        // include the edge so we get the item counts properly
        final int endY = y + 14 + 52 + 1;
        graphics.enableScissor(startX, startY, endX, endY);
        renderMatrix(graphics, input);
        renderMatrixSlots(graphics, mouseX, mouseY, input);
        graphics.disableScissor();
    }

    private void renderMatrix(final GuiGraphicsExtractor graphics, final boolean input) {
        final int xx = x + INSET_PADDING + (!input ? INDIVIDUAL_PROCESSING_MATRIX_SIZE + 2 : 0);
        final int startY = y + PROCESSING_INSET_Y_PADDING - INDIVIDUAL_PROCESSING_MATRIX_SIZE;
        final int endY = y + PROCESSING_INSET_Y_PADDING + 4 + INDIVIDUAL_PROCESSING_MATRIX_SIZE;
        final int scrollbarOffset = scrollbar != null ? (int) scrollbar.getOffset() : 0;
        final int scrollbarOffsetCorrected = scrollbar != null && scrollbar.isSmoothScrolling()
            ? scrollbarOffset
            : scrollbarOffset * PROCESSING_MATRIX_SLOT_SIZE;
        for (int i = 0; i < 9; ++i) {
            final int yy = (y + PROCESSING_INSET_Y_PADDING + 4)
                + (i * INDIVIDUAL_PROCESSING_MATRIX_SIZE)
                - scrollbarOffsetCorrected;
            if (yy < startY || yy > endY) {
                continue;
            }
            graphics.blitSprite(
                GUI_TEXTURED,
                PROCESSING_MATRIX,
                xx,
                yy,
                INDIVIDUAL_PROCESSING_MATRIX_SIZE,
                INDIVIDUAL_PROCESSING_MATRIX_SIZE
            );
        }
    }

    private void renderMatrixSlots(final GuiGraphicsExtractor graphics,
                                   final int mouseX,
                                   final int mouseY,
                                   final boolean input) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        for (final ResourceSlot resourceSlot : menu.getResourceSlots()) {
            if (resourceSlot.isActive()
                && resourceSlot instanceof ProcessingMatrixResourceSlot matrixSlot
                && matrixSlot.isInput() == input) {
                renderMatrixSlot(graphics, mouseX, mouseY, resourceSlot, matrixSlot);
            }
        }
        graphics.pose().popMatrix();
    }

    private void renderMatrixSlot(final GuiGraphicsExtractor graphics,
                                  final int mouseX,
                                  final int mouseY,
                                  final ResourceSlot resourceSlot,
                                  final ProcessingMatrixResourceSlot matrixSlot) {
        final PlatformResourceKey resource = matrixSlot.getResource();
        if (resource != null && menu.getRepository().isSticky(resource)) {
            AbstractGridScreen.renderSlotBackground(
                graphics,
                resourceSlot.x,
                resourceSlot.y,
                false,
                AutocraftableResourceHint.AUTOCRAFTABLE.getColor()
            );
        }
        final boolean hovering = mouseX >= resourceSlot.x + leftPos
            && mouseX < resourceSlot.x + leftPos + 16
            && mouseY >= resourceSlot.y + topPos
            && mouseY < resourceSlot.y + topPos + 16;
        final boolean interact = hovering && canInteractWithResourceSlot(resourceSlot, mouseX, mouseY);
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightBack(graphics, resourceSlot.x, resourceSlot.y);
        }
        ResourceSlotRendering.render(graphics, resourceSlot);
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightFront(graphics, resourceSlot.x, resourceSlot.y);
        }
    }

    @Override
    public boolean canInteractWithResourceSlot(final ResourceSlot resourceSlot,
                                               final double mouseX,
                                               final double mouseY) {
        final int insetContentX = x + INSET_PADDING + (
            resourceSlot instanceof ProcessingMatrixResourceSlot matrixSlot && !matrixSlot.isInput()
                ? INDIVIDUAL_PROCESSING_MATRIX_SIZE + 2
                : 0);
        final int insetContentY = y + INSET_PADDING;
        return mouseX >= insetContentX
            && mouseX < insetContentX + INDIVIDUAL_PROCESSING_MATRIX_SIZE
            && mouseY >= insetContentY + PROCESSING_INSET_Y_PADDING
            && mouseY < insetContentY + PROCESSING_INSET_Y_PADDING + INDIVIDUAL_PROCESSING_MATRIX_SIZE;
    }

    @Override
    public void extractLabels(final GuiGraphicsExtractor graphics, final Font font, final int mouseX,
                              final int mouseY) {
        final int xx = x - leftPos + INSET_PADDING;
        final int yy = y - topPos - 1 + INSET_PADDING;
        graphics.text(font, INPUTS, xx, yy, -12566464, false);
        graphics.text(font, OUTPUTS, xx + 56, yy, -12566464, false);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent e, final boolean doubleClick) {
        return scrollbar != null && scrollbar.mouseClicked(e, doubleClick);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent e) {
        return scrollbar != null && scrollbar.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double mouseZ, final double delta) {
        if (!(mouseX >= x && (mouseX < x + INSET_WIDTH) && mouseY > y && (mouseY < y + INSET_HEIGHT))) {
            return false;
        }
        return scrollbar != null && scrollbar.mouseScrolled(mouseX, mouseY, mouseZ, delta);
    }

    @Override
    public void patternTypeChanged(final PatternType newPatternType) {
        if (scrollbar != null) {
            scrollbar.visible = isScrollbarVisible(menu);
        }
    }
}

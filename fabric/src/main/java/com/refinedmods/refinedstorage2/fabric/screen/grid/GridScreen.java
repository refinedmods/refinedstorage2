package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.core.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.core.query.lexer.SyntaxHighlighterColors;
import com.refinedmods.refinedstorage2.core.util.Quantities;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.FabricItemGridStack;
import com.refinedmods.refinedstorage2.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.fabric.screen.BaseScreen;
import com.refinedmods.refinedstorage2.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.ItemStacks;
import com.refinedmods.refinedstorage2.fabric.util.LastModifiedUtil;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridScreen extends BaseScreen<GridScreenHandler> {
    private static final Logger LOGGER = LogManager.getLogger(GridScreen.class);

    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/grid.png");

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;
    private static final int COLUMNS = 9;

    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final int SELECTION_SLOT_COLOR = -2130706433;

    private ScrollbarWidget scrollbar;
    private GridSearchBoxWidget searchField;
    private int totalRows;
    private int visibleRows;
    private int gridSlotNumber;

    public GridScreen(GridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        handler.setSizeChangedListener(this::init);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 75;
        this.backgroundWidth = 227;
        this.backgroundHeight = 176;
    }

    private static GridExtractMode getExtractMode(int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.CURSOR_HALF;
        }
        if (hasShiftDown()) {
            return GridExtractMode.PLAYER_INVENTORY_STACK;
        }
        return GridExtractMode.CURSOR_STACK;
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE : GridInsertMode.ENTIRE_STACK;
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing grid screen");

        this.visibleRows = calculateVisibleRows();
        this.backgroundHeight = TOP_HEIGHT + (visibleRows * 18) + BOTTOM_HEIGHT;
        this.playerInventoryTitleY = backgroundHeight - BOTTOM_HEIGHT + 4;

        super.init();

        if (searchField == null) {
            searchField = new GridSearchBoxWidget(textRenderer, x + 80 + 1, y + 6 + 1, 88 - 6, new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS));
        } else {
            searchField.x = x + 80 + 1;
            searchField.y = y + 6 + 1;
        }
        getScreenHandler().setSearchBox(searchField);

        getScreenHandler().addSlots(backgroundHeight - BOTTOM_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(client, x + 174, y + 20, 12, (visibleRows * 18) - 2);
        this.scrollbar.setScrollAnimation(Rs2Config.get().getGrid().isSmoothScrolling());
        this.getScreenHandler().getItemView().setListener(this::stacksChanged);
        stacksChanged();

        children.add(scrollbar);
        addButton(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SortingDirectionSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SortingTypeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SizeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SearchBoxModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
    }

    @Override
    public void tick() {
        super.tick();

        String newValue = getScreenHandler().getSearchBoxMode().getSearchBoxValue();
        if (searchField != null && newValue != null && !searchField.getText().equals(newValue)) {
            searchField.setText(newValue);
        }
    }

    private void stacksChanged() {
        totalRows = (int) Math.ceil((float) getScreenHandler().getItemView().getStacks().size() / (float) COLUMNS);

        scrollbar.setEnabled(totalRows > visibleRows);

        int rowsExcludingVisibleOnes = totalRows - visibleRows;
        scrollbar.setMaxOffset(scrollbar.isScrollAnimation() ? ((rowsExcludingVisibleOnes) * 18) : rowsExcludingVisibleOnes);
    }

    private int calculateVisibleRows() {
        int screenSpaceAvailable = height - TOP_HEIGHT - BOTTOM_HEIGHT;
        int maxRows = getMaxRows();

        return Math.max(3, Math.min((screenSpaceAvailable / 18) - 3, maxRows));
    }

    private int getMaxRows() {
        switch (getScreenHandler().getSize()) {
            case STRETCH:
                return Rs2Config.get().getGrid().getMaxRowsStretch();
            case SMALL:
                return 3;
            case MEDIUM:
                return 5;
            case LARGE:
                return 8;
            default:
                return Rs2Config.get().getGrid().getMaxRowsStretch();
        }
    }

    private boolean isOverStorageArea(int mouseX, int mouseY) {
        mouseX -= x;
        mouseY -= y;

        return mouseX >= 7 && mouseY >= TOP_HEIGHT
                && mouseX <= 168 && mouseY <= TOP_HEIGHT + (visibleRows * 18);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ScreenUtil.drawVersionInformation(matrices, textRenderer);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth - 34, TOP_HEIGHT);

        for (int row = 0; row < visibleRows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == visibleRows - 1) {
                textureY = 55;
            }

            drawTexture(matrices, x, y + TOP_HEIGHT + (18 * row), 0, textureY, backgroundWidth - 34, 18);
        }

        drawTexture(matrices, x, y + TOP_HEIGHT + (18 * visibleRows), 0, 73, backgroundWidth - 34, BOTTOM_HEIGHT);

        gridSlotNumber = -1;

        setScissor(x + 7, y + TOP_HEIGHT, 18 * COLUMNS, visibleRows * 18);
        for (int row = 0; row < Math.max(totalRows, visibleRows); ++row) {
            renderRow(matrices, mouseX, mouseY, x, y, row);
        }
        disableScissor();

        if (gridSlotNumber != -1 && isOverStorageArea(mouseX, mouseY)) {
            renderTooltip(matrices, mouseX, mouseY);
        }
    }

    private void renderRow(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int row) {
        int scrollbarOffset = (int) scrollbar.getOffset();
        if (!scrollbar.isScrollAnimation()) {
            scrollbarOffset *= 18;
        }

        int rowX = x + 7;
        int rowY = y + TOP_HEIGHT + (row * 18) - scrollbarOffset;

        boolean isOutOfFrame = (rowY < y + TOP_HEIGHT - 18) || (rowY > y + TOP_HEIGHT + (visibleRows * 18));
        if (isOutOfFrame) {
            return;
        }

        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrices, rowX, rowY, 0, 238, 162, 18);

        for (int column = 0; column < COLUMNS; ++column) {
            renderColumnInRow(matrices, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private void renderColumnInRow(MatrixStack matrices, int mouseX, int mouseY, int rowX, int rowY, int idx, int column) {
        GridView<Rs2ItemStack> view = getScreenHandler().getItemView();

        int slotX = rowX + 1 + (column * 18);
        int slotY = rowY + 1;

        FabricItemGridStack stack = null;
        if (idx < view.getStacks().size()) {
            stack = (FabricItemGridStack) view.getStacks().get(idx);
            renderStack(matrices, slotX, slotY, stack);
        }

        if (!getScreenHandler().isActive()) {
            renderDisabledSlot(matrices, slotX, slotY);
        } else if (mouseX >= slotX && mouseY >= slotY && mouseX <= slotX + 16 && mouseY <= slotY + 16 && isOverStorageArea(mouseX, mouseY)) {
            renderSelection(matrices, slotX, slotY);
            if (stack != null) {
                gridSlotNumber = idx;
            }
        }
    }

    private void renderDisabledSlot(MatrixStack matrices, int slotX, int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR, DISABLED_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderSelection(MatrixStack matrices, int slotX, int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, SELECTION_SLOT_COLOR, SELECTION_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderStack(MatrixStack matrices, int slotX, int slotY, FabricItemGridStack stack) {
        setZOffset(100);
        itemRenderer.zOffset = 100.0F;

        itemRenderer.renderInGuiWithOverrides(client.player, stack.getMcStack(), slotX, slotY);

        String text = stack.isZeroed() ? "0" : String.valueOf(stack.getAmount());
        Integer color = stack.isZeroed() ? Formatting.RED.getColorValue() : Formatting.WHITE.getColorValue();

        renderAmount(matrices, slotX, slotY, text, color);

        setZOffset(0);
        itemRenderer.zOffset = 0.0F;
    }

    private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        GridView<Rs2ItemStack> view = getScreenHandler().getItemView();
        FabricItemGridStack stack = (FabricItemGridStack) view.getStacks().get(gridSlotNumber);

        if (!Rs2Config.get().getGrid().isDetailedTooltip()) {
            renderTooltip(matrices, stack.getMcStack(), mouseX, mouseY);
        } else {
            List<OrderedText> lines = Lists.transform(getTooltipFromItem(stack.getMcStack()), Text::asOrderedText);
            List<OrderedText> smallLines = new ArrayList<>();
            smallLines.add(Rs2Mod.createTranslation("misc", "total", stack.isZeroed() ? "0" : Quantities.format(stack.getAmount())).formatted(Formatting.GRAY).asOrderedText());

            view.getTrackerEntry(stack.getStack()).ifPresent(entry -> smallLines.add(LastModifiedUtil.getText(entry.getTime(), entry.getName()).formatted(Formatting.GRAY).asOrderedText()));

            renderTooltipWithSmallText(matrices, lines, smallLines, mouseX, mouseY);
        }
    }

    private void renderAmount(MatrixStack matrixStack, int x, int y, String amount, int color) {
        boolean large = this.client.forcesUnicodeFont() || Rs2Config.get().getGrid().isLargeFont();

        matrixStack.push();
        matrixStack.translate(x, y, 300);

        if (!large) {
            matrixStack.scale(0.5F, 0.5F, 1);
        }

        textRenderer.drawWithShadow(matrixStack, amount, (float) (large ? 16 : 30) - textRenderer.getWidth(amount), large ? 8 : 22, color);

        matrixStack.pop();
    }

    private void renderTooltipWithSmallText(MatrixStack matrixStack, List<? extends OrderedText> lines, List<? extends OrderedText> smallLines, int x, int y) {
        if (lines.isEmpty()) {
            return;
        }

        float smallTextScale = client.forcesUnicodeFont() ? 1F : 0.7F;

        int tooltipWidth = 0;

        for (OrderedText text : lines) {
            int textWidth = textRenderer.getWidth(text);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }

        for (OrderedText text : smallLines) {
            int textWidth = (int) (textRenderer.getWidth(text) * smallTextScale);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }

        int tooltipX = x + 12;
        int tooltipY = y - 12;
        int tooltipHeight = 8;

        if (lines.size() > 1) {
            tooltipHeight += 2 + (lines.size() - 1) * 10;
        }

        tooltipHeight += smallLines.size() * 10;

        if (tooltipX + tooltipWidth > width) {
            tooltipX -= 28 + tooltipWidth;
        }

        if (tooltipY + tooltipHeight + 6 > height) {
            tooltipY = height - tooltipHeight - 6;
        }

        matrixStack.push();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, 1347420415, 1347420415);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrixStack.translate(0.0D, 0.0D, 400.0D);

        for (int i = 0; i < lines.size(); ++i) {
            OrderedText text = lines.get(i);
            if (text != null) {
                textRenderer.draw(text, (float) tooltipX, (float) tooltipY, -1, true, matrix4f, immediate, false, 0, 15728880);
            }

            if (i == 0) {
                tooltipY += 2;
            }

            tooltipY += 10;
        }

        for (OrderedText smallLine : smallLines) {
            matrixStack.push();
            matrixStack.scale(smallTextScale, smallTextScale, 1);

            textRenderer.draw(smallLine, (float) tooltipX / smallTextScale, (float) tooltipY / smallTextScale, -1, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
            matrixStack.pop();

            tooltipY += 9;
        }

        immediate.draw();
        matrixStack.pop();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        scrollbar.render(matrices, mouseX, mouseY, partialTicks);
        searchField.render(matrices, 0, 0, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        ItemStack cursorStack = playerInventory.getCursorStack();

        if (!getScreenHandler().getItemView().getStacks().isEmpty() && gridSlotNumber >= 0 && cursorStack.isEmpty()) {
            mouseClickedInGridWithoutItem(clickedButton);
            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY) && !cursorStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGridWithItem(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private void mouseClickedInGridWithItem(int clickedButton) {
        getScreenHandler().onInsertFromCursor(getInsertMode(clickedButton));
    }

    private void mouseClickedInGridWithoutItem(int clickedButton) {
        GridStack<Rs2ItemStack> stack = getScreenHandler().getItemView().getStacks().get(gridSlotNumber);
        getScreenHandler().onExtract(stack.getStack(), getExtractMode(clickedButton));
    }

    @Override
    public void mouseMoved(double mx, double my) {
        scrollbar.mouseMoved(mx, my);
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return scrollbar.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        boolean up = delta > 0;

        if (isOverStorageArea((int) x, (int) y) && gridSlotNumber >= 0) {
            mouseScrolledOnStorageArea(up);
        } else if (focusedSlot != null && focusedSlot.hasStack()) {
            mouseScrolledOnInventoryArea(up);
        }

        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    private void mouseScrolledOnInventoryArea(boolean up) {
        getScreenHandler().getItemView().setPreventSorting(true);

        Rs2ItemStack stack = ItemStacks.ofItemStack(focusedSlot.getStack());
        int slot = ((SlotAccessor) focusedSlot).getIndex();
        GridScrollMode mode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (mode == null) {
            return;
        }

        getScreenHandler().onScroll(stack, slot, mode);
    }

    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(boolean up) {
        if (hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    private void mouseScrolledOnStorageArea(boolean up) {
        getScreenHandler().getItemView().setPreventSorting(true);

        Rs2ItemStack stack = getScreenHandler().getItemView().getStacks().get(gridSlotNumber).getStack();
        int slot = playerInventory.getSlotWithStack(ItemStacks.toItemStack(stack));
        GridScrollMode mode = getScrollModeWhenScrollingOnGridArea(up);
        if (mode == null) {
            return;
        }

        getScreenHandler().onScroll(stack, slot, mode);
    }

    private GridScrollMode getScrollModeWhenScrollingOnGridArea(boolean up) {
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();
        if (shift && ctrl) {
            return null;
        }

        if (up) {
            if (shift) {
                return GridScrollMode.INVENTORY_TO_GRID;
            }
        } else {
            if (shift) {
                return GridScrollMode.GRID_TO_INVENTORY;
            } else if (ctrl) {
                return GridScrollMode.GRID_TO_CURSOR;
            }
        }

        return null;
    }

    @Override
    public boolean charTyped(char unknown1, int unknown2) {
        return searchField.charTyped(unknown1, unknown2) || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchField.keyPressed(key, scanCode, modifiers) || searchField.isActive()) {
            return true;
        }

        if (hasShiftDown() && Rs2Config.get().getGrid().isPreventSortingWhileShiftIsDown()) {
            getScreenHandler().getItemView().setPreventSorting(true);
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (getScreenHandler().getItemView().isPreventSorting()) {
            getScreenHandler().getItemView().setPreventSorting(false);
            getScreenHandler().getItemView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}

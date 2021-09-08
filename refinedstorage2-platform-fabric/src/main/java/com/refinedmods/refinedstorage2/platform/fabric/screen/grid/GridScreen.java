package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.core.LastModified;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.screen.BaseScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighterColors;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GridScreen<S extends Rs2Stack, T extends GridScreenHandler<S>> extends BaseScreen<T> {
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

    public GridScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        handler.setSizeChangedListener(this::init);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 75;
        this.backgroundWidth = 227;
        this.backgroundHeight = 176;
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

        this.scrollbar = new ScrollbarWidget(x + 174, y + 20, 12, (visibleRows * 18) - 2);
        this.scrollbar.setScrollAnimation(Rs2Config.get().getGrid().isSmoothScrolling());
        this.getScreenHandler().getView().setListener(this::stacksChanged);
        stacksChanged();

        addSelectableChild(scrollbar);
        addSelectableChild(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SortingDirectionSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SortingTypeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SizeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new SearchBoxModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        String newValue = getScreenHandler().getSearchBoxMode().getSearchBoxValue();
        if (searchField != null && newValue != null && !searchField.getText().equals(newValue)) {
            searchField.setText(newValue);
        }
    }

    private void stacksChanged() {
        totalRows = (int) Math.ceil((float) getScreenHandler().getView().getStacks().size() / (float) COLUMNS);

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
        return switch (getScreenHandler().getSize()) {
            case STRETCH -> Rs2Config.get().getGrid().getMaxRowsStretch();
            case SMALL -> 3;
            case MEDIUM -> 5;
            case LARGE -> 8;
            case EXTRA_LARGE -> 12;
        };
    }

    private boolean isOverStorageArea(int mouseX, int mouseY) {
        mouseX -= x;
        mouseY -= y;

        return mouseX >= 7 && mouseY >= TOP_HEIGHT
                && mouseX <= 168 && mouseY <= TOP_HEIGHT + (visibleRows * 18);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        ScreenUtil.drawVersionInformation(matrices, textRenderer);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

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

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        drawTexture(matrices, rowX, rowY, 0, 238, 162, 18);

        for (int column = 0; column < COLUMNS; ++column) {
            renderColumnInRow(matrices, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private void renderColumnInRow(MatrixStack matrices, int mouseX, int mouseY, int rowX, int rowY, int idx, int column) {
        GridView<S> view = getScreenHandler().getView();

        int slotX = rowX + 1 + (column * 18);
        int slotY = rowY + 1;

        GridStack<S> stack = null;
        if (idx < view.getStacks().size()) {
            stack = view.getStacks().get(idx);
            renderStackWithAmount(matrices, slotX, slotY, stack);
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

    private void renderStackWithAmount(MatrixStack matrices, int slotX, int slotY, GridStack<S> stack) {
        renderStack(matrices, slotX, slotY, stack);

        String text = getAmount(stack);
        Integer color = stack.isZeroed() ? Formatting.RED.getColorValue() : Formatting.WHITE.getColorValue();

        renderAmount(matrices, slotX, slotY, text, color);
    }

    protected abstract void renderStack(MatrixStack matrices, int slotX, int slotY, GridStack<S> stack);

    protected abstract String getAmount(GridStack<S> stack);

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

    private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        GridView<S> view = getScreenHandler().getView();
        GridStack<S> stack = view.getStacks().get(gridSlotNumber);

        List<OrderedText> lines = Lists.transform(getTooltip(stack), Text::asOrderedText);

        if (!Rs2Config.get().getGrid().isDetailedTooltip()) {
            renderOrderedTooltip(matrices, lines, mouseX, mouseY);
        } else {
            List<OrderedText> smallLines = new ArrayList<>();
            smallLines.add(Rs2Mod.createTranslation("misc", "total", getAmount(stack)).formatted(Formatting.GRAY).asOrderedText());

            view.getTrackerEntry(stack.getStack()).ifPresent(entry -> smallLines.add(getLastModifiedText(entry).formatted(Formatting.GRAY).asOrderedText()));

            renderTooltipWithSmallText(matrices, lines, smallLines, mouseX, mouseY);
        }
    }

    protected abstract List<Text> getTooltip(GridStack<S> stack);

    private MutableText getLastModifiedText(StorageTracker.Entry entry) {
        LastModified lastModified = LastModified.calculate(entry.time(), System.currentTimeMillis());

        if (lastModified.type() == LastModified.Type.JUST_NOW) {
            return Rs2Mod.createTranslation("misc", "last_modified.just_now", entry.name());
        }

        String translationKey = lastModified.type().toString().toLowerCase();
        boolean plural = lastModified.amount() != 1;
        if (plural) {
            translationKey += "s";
        }

        return Rs2Mod.createTranslation("misc", "last_modified." + translationKey, lastModified.amount(), entry.name());
    }

    protected void renderAmount(MatrixStack matrixStack, int x, int y, String amount, int color) {
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
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
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrixStack.translate(0.0D, 0.0D, 400.0D);

        for (int i = 0; i < lines.size(); ++i) {
            OrderedText text = lines.get(i);
            if (text != null) {
                textRenderer.draw(text, tooltipX, tooltipY, -1, true, matrix4f, immediate, false, 0, 15728880);
            }

            tooltipY += 12;
        }

        for (OrderedText smallLine : smallLines) {
            matrixStack.push();
            matrixStack.scale(smallTextScale, smallTextScale, 1);

            textRenderer.draw(smallLine, tooltipX / smallTextScale, tooltipY / smallTextScale, -1, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
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

        ItemStack cursorStack = getScreenHandler().getCursorStack();

        if (!getScreenHandler().getView().getStacks().isEmpty() && gridSlotNumber >= 0 && cursorStack.isEmpty()) {
            mouseClickedInGrid(clickedButton, getScreenHandler().getView().getStacks().get(gridSlotNumber));
            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY) && !cursorStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGrid(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    protected abstract void mouseClickedInGrid(int clickedButton);

    protected abstract void mouseClickedInGrid(int clickedButton, GridStack<S> stack);

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
            mouseScrolledInGrid(up);
        } else if (focusedSlot != null && focusedSlot.hasStack()) {
            mouseScrolledInInventory(up);
        }

        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    private void mouseScrolledInInventory(boolean up) {
        getScreenHandler().getView().setPreventSorting(true);
        Rs2ItemStack stack = Rs2PlatformApiFacade.INSTANCE.itemStackConversion().toDomain(focusedSlot.getStack());
        int slotIndex = ((SlotAccessor) focusedSlot).getIndex();
        mouseScrolledInInventory(up, stack, slotIndex);
    }

    protected abstract void mouseScrolledInInventory(boolean up, Rs2ItemStack stack, int slotIndex);

    private void mouseScrolledInGrid(boolean up) {
        getScreenHandler().getView().setPreventSorting(true);
        GridStack<S> stack = getScreenHandler().getView().getStacks().get(gridSlotNumber);
        mouseScrolledInGrid(up, stack);
    }

    protected abstract void mouseScrolledInGrid(boolean up, GridStack<S> stack);

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
            getScreenHandler().getView().setPreventSorting(true);
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (getScreenHandler().getView().isPreventSorting()) {
            getScreenHandler().getView().setPreventSorting(false);
            getScreenHandler().getView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}

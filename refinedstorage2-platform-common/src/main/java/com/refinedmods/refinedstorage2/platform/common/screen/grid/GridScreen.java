package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.util.LastModified;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.BaseScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighterColors;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class GridScreen<R, T extends GridContainerMenu<R>> extends BaseScreen<T> {
    private static final Logger LOGGER = LogManager.getLogger(GridScreen.class);

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/grid.png");

    private static final int MODIFIED_JUST_NOW_MAX_SECONDS = 10;

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;
    private static final int COLUMNS = 9;

    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final int SELECTION_SLOT_COLOR = -2130706433;

    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    private ScrollbarWidget scrollbar;
    private GridSearchBoxWidget<R> searchField;
    private int totalRows;
    private int visibleRows;
    private int gridSlotNumber;

    protected GridScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        menu.setSizeChangedListener(this::init);

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 75;
        this.imageWidth = 227;
        this.imageHeight = 176;
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing grid screen");

        this.visibleRows = calculateVisibleRows();
        this.imageHeight = TOP_HEIGHT + (visibleRows * 18) + BOTTOM_HEIGHT;
        this.inventoryLabelY = imageHeight - BOTTOM_HEIGHT + 4;

        super.init();

        if (searchField == null) {
            searchField = new GridSearchBoxWidget<>(
                    font,
                    leftPos + 80 + 1,
                    topPos + 6 + 1,
                    88 - 6,
                    new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS),
                    menu.getView(),
                    new GridQueryParserImpl<>(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS, GridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING),
                    SEARCH_FIELD_HISTORY
            );
        } else {
            searchField.x = leftPos + 80 + 1;
            searchField.y = topPos + 6 + 1;
        }
        getMenu().setSearchBox(searchField);

        getMenu().addSlots(imageHeight - BOTTOM_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(leftPos + 174, topPos + 20, 12, (visibleRows * 18) - 2);
        this.scrollbar.setScrollAnimation(Platform.INSTANCE.getConfig().getGrid().isSmoothScrolling());
        this.getMenu().getView().setListener(this::resourcesChanged);
        resourcesChanged();

        addWidget(scrollbar);
        addWidget(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new SortingDirectionSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new SortingTypeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new SizeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new AutoSelectedSideButtonWidget(getMenu(), this::renderComponentTooltip));

        OrderedRegistry<ResourceLocation, GridSynchronizer> synchronizerRegistry = PlatformApi.INSTANCE.getGridSynchronizerRegistry();
        if (!synchronizerRegistry.isEmpty()) {
            addSideButton(new SynchronizationSideButtonWidget(getMenu(), this::renderComponentTooltip, synchronizerRegistry.getAll()));
            searchField.addListener(this::trySynchronizeFromGrid);
        }
    }

    private void trySynchronizeFromGrid(String text) {
        getMenu().getSynchronizer().synchronizeFromGrid(text);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        trySynchronizeToGrid();
    }

    private void trySynchronizeToGrid() {
        if (searchField == null) {
            return;
        }
        String text = getMenu().getSynchronizer().getTextToSynchronizeToGrid();
        if (text == null || searchField.getValue().equals(text)) {
            return;
        }
        searchField.setValue(text);
    }

    private void resourcesChanged() {
        totalRows = (int) Math.ceil((float) getMenu().getView().getAll().size() / (float) COLUMNS);

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
        return switch (getMenu().getSize()) {
            case STRETCH -> Platform.INSTANCE.getConfig().getGrid().getMaxRowsStretch();
            case SMALL -> 3;
            case MEDIUM -> 5;
            case LARGE -> 8;
            case EXTRA_LARGE -> 12;
        };
    }

    private boolean isOverStorageArea(int mouseX, int mouseY) {
        mouseX -= leftPos;
        mouseY -= topPos;

        return mouseX >= 7 && mouseY >= TOP_HEIGHT && mouseX <= 168 && mouseY <= TOP_HEIGHT + (visibleRows * 18);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth - 34, TOP_HEIGHT);

        for (int row = 0; row < visibleRows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == visibleRows - 1) {
                textureY = 55;
            }

            blit(poseStack, x, y + TOP_HEIGHT + (18 * row), 0, textureY, imageWidth - 34, 18);
        }

        blit(poseStack, x, y + TOP_HEIGHT + (18 * visibleRows), 0, 73, imageWidth - 34, BOTTOM_HEIGHT);

        gridSlotNumber = -1;

        setScissor(x + 7, y + TOP_HEIGHT, 18 * COLUMNS, visibleRows * 18);
        for (int row = 0; row < Math.max(totalRows, visibleRows); ++row) {
            renderRow(poseStack, mouseX, mouseY, x, y, row);
        }
        disableScissor();

        if (gridSlotNumber != -1 && isOverStorageArea(mouseX, mouseY)) {
            renderTooltipWithMaybeSmallLines(poseStack, mouseX, mouseY);
        }
    }

    private void renderRow(PoseStack poseStack, int mouseX, int mouseY, int x, int y, int row) {
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

        blit(poseStack, rowX, rowY, 0, 238, 162, 18);

        for (int column = 0; column < COLUMNS; ++column) {
            renderColumnInRow(poseStack, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private void renderColumnInRow(PoseStack poseStack, int mouseX, int mouseY, int rowX, int rowY, int idx, int column) {
        GridView<R> view = getMenu().getView();

        int slotX = rowX + 1 + (column * 18);
        int slotY = rowY + 1;

        GridResource<R> resource = null;
        if (idx < view.getAll().size()) {
            resource = view.getAll().get(idx);
            renderResourceWithAmount(poseStack, slotX, slotY, resource);
        }

        if (!getMenu().isActive()) {
            renderDisabledSlot(poseStack, slotX, slotY);
        } else if (mouseX >= slotX && mouseY >= slotY && mouseX <= slotX + 16 && mouseY <= slotY + 16 && isOverStorageArea(mouseX, mouseY)) {
            renderSelection(poseStack, slotX, slotY);
            if (resource != null) {
                gridSlotNumber = idx;
            }
        }
    }

    private void renderResourceWithAmount(PoseStack poseStack, int slotX, int slotY, GridResource<R> resource) {
        renderResource(poseStack, slotX, slotY, resource);
        renderAmount(poseStack, slotX, slotY, resource);
    }

    private void renderAmount(PoseStack poseStack, int slotX, int slotY, GridResource<R> resource) {
        String text = getAmount(resource);
        Integer color = resource.isZeroed() ? ChatFormatting.RED.getColor() : ChatFormatting.WHITE.getColor();
        renderAmount(poseStack, slotX, slotY, text, color);
    }

    protected abstract void renderResource(PoseStack poseStack, int slotX, int slotY, GridResource<R> resource);

    protected abstract String getAmount(GridResource<R> resource);

    protected abstract String getAmountInTooltip(GridResource<R> resource);

    private void renderDisabledSlot(PoseStack poseStack, int slotX, int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(poseStack, slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR, DISABLED_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderSelection(PoseStack poseStack, int slotX, int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(poseStack, slotX, slotY, slotX + 16, slotY + 16, SELECTION_SLOT_COLOR, SELECTION_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderTooltipWithMaybeSmallLines(PoseStack poseStack, int mouseX, int mouseY) {
        GridView<R> view = getMenu().getView();
        GridResource<R> resource = view.getAll().get(gridSlotNumber);

        List<FormattedCharSequence> lines = Lists.transform(getTooltip(resource), Component::getVisualOrderText);

        if (!Platform.INSTANCE.getConfig().getGrid().isDetailedTooltip()) {
            renderTooltip(poseStack, lines, mouseX, mouseY);
        } else {
            List<FormattedCharSequence> smallLines = new ArrayList<>();
            smallLines.add(createTranslation("misc", "total", getAmountInTooltip(resource)).withStyle(ChatFormatting.GRAY).getVisualOrderText());

            view.getTrackedResource(resource.getResourceAmount().getResource()).ifPresent(entry -> smallLines.add(
                    getLastModifiedText(entry).withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ));

            renderTooltipWithSmallText(poseStack, lines, smallLines, mouseX, mouseY);
        }
    }

    protected abstract List<Component> getTooltip(GridResource<R> resource);

    private MutableComponent getLastModifiedText(TrackedResource trackedResource) {
        LastModified lastModified = LastModified.calculate(trackedResource.getTime(), System.currentTimeMillis());

        if (isModifiedJustNow(lastModified)) {
            return createTranslation("misc", "last_modified.just_now", trackedResource.getSourceName());
        }

        String translationKey = lastModified.type().toString().toLowerCase();
        boolean plural = lastModified.amount() != 1;
        if (plural) {
            translationKey += "s";
        }

        return createTranslation("misc", "last_modified." + translationKey, lastModified.amount(), trackedResource.getSourceName());
    }

    private boolean isModifiedJustNow(LastModified lastModified) {
        return lastModified.type() == LastModified.Type.SECOND && lastModified.amount() <= MODIFIED_JUST_NOW_MAX_SECONDS;
    }

    protected void renderAmount(PoseStack poseStack, int x, int y, String amount, int color) {
        boolean large = this.minecraft.isEnforceUnicode() || Platform.INSTANCE.getConfig().getGrid().isLargeFont();

        poseStack.pushPose();
        poseStack.translate(x, y, 300);

        if (!large) {
            poseStack.scale(0.5F, 0.5F, 1);
        }

        font.drawShadow(poseStack, amount, (float) (large ? 16 : 30) - font.width(amount), large ? 8 : 22, color);

        poseStack.popPose();
    }

    private void renderTooltipWithSmallText(PoseStack poseStack, List<? extends FormattedCharSequence> lines, List<? extends FormattedCharSequence> smallLines, int x, int y) {
        if (lines.isEmpty()) {
            return;
        }

        float smallTextScale = minecraft.isEnforceUnicode() ? 1F : 0.7F;

        int tooltipWidth = 0;

        for (FormattedCharSequence text : lines) {
            int textWidth = font.width(text);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }

        for (FormattedCharSequence text : smallLines) {
            int textWidth = (int) (font.width(text) * smallTextScale);
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

        poseStack.pushPose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
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
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0D, 0.0D, 400.0D);

        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence text = lines.get(i);
            if (text != null) {
                font.drawInBatch(text, tooltipX, tooltipY, -1, true, matrix4f, immediate, false, 0, 15728880);
            }

            tooltipY += 12;
        }

        for (FormattedCharSequence smallLine : smallLines) {
            poseStack.pushPose();
            poseStack.scale(smallTextScale, smallTextScale, 1);

            font.drawInBatch(smallLine, tooltipX / smallTextScale, tooltipY / smallTextScale, -1, true, poseStack.last().pose(), immediate, false, 0, 15728880);
            poseStack.popPose();

            tooltipY += 9;
        }

        immediate.endBatch();
        poseStack.popPose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTooltip(poseStack, mouseX, mouseY);

        scrollbar.render(poseStack, mouseX, mouseY, partialTicks);
        searchField.render(poseStack, 0, 0, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        ItemStack cursorStack = getMenu().getCarried();

        if (!getMenu().getView().getAll().isEmpty() && gridSlotNumber >= 0 && cursorStack.isEmpty()) {
            mouseClickedInGrid(clickedButton, getMenu().getView().getAll().get(gridSlotNumber));
            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY) && !cursorStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGrid(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    protected abstract void mouseClickedInGrid(int clickedButton);

    protected abstract void mouseClickedInGrid(int clickedButton, GridResource<R> resource);

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
        } else if (hoveredSlot != null && hoveredSlot.hasItem()) {
            mouseScrolledInInventory(up);
        }

        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    private void mouseScrolledInInventory(boolean up) {
        getMenu().getView().setPreventSorting(true);
        int slotIndex = hoveredSlot.getContainerSlot();
        mouseScrolledInInventory(up, hoveredSlot.getItem(), slotIndex);
    }

    protected abstract void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex);

    private void mouseScrolledInGrid(boolean up) {
        getMenu().getView().setPreventSorting(true);
        GridResource<R> resource = getMenu().getView().getAll().get(gridSlotNumber);
        mouseScrolledInGrid(up, resource);
    }

    protected abstract void mouseScrolledInGrid(boolean up, GridResource<R> resource);

    @Override
    public boolean charTyped(char unknown1, int unknown2) {
        return searchField.charTyped(unknown1, unknown2) || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchField.keyPressed(key, scanCode, modifiers) || searchField.canConsumeInput()) {
            return true;
        }

        if (hasShiftDown() && Platform.INSTANCE.getConfig().getGrid().isPreventSortingWhileShiftIsDown()) {
            getMenu().getView().setPreventSorting(true);
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (getMenu().getView().isPreventSorting()) {
            getMenu().getView().setPreventSorting(false);
            getMenu().getView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}

package com.refinedmods.refinedstorage.platform.common.grid.screen;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.view.PlatformGridResource;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.grid.NoopGridSynchronizer;
import com.refinedmods.refinedstorage.platform.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.widget.History;
import com.refinedmods.refinedstorage.platform.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighterColors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNullElse;

public abstract class AbstractGridScreen<T extends AbstractGridContainerMenu> extends AbstractStretchingScreen<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridScreen.class);

    private static final int MODIFIED_JUST_NOW_MAX_SECONDS = 10;
    private static final int COLUMNS = 9;
    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    protected final int bottomHeight;
    @Nullable
    GridSearchBoxWidget searchField;

    private int totalRows;
    private int currentGridSlotIndex;

    protected AbstractGridScreen(final T menu,
                                 final Inventory playerInventory,
                                 final Component text,
                                 final int bottomHeight) {
        super(menu, playerInventory, text);
        this.bottomHeight = bottomHeight;
    }

    @Override
    protected void init(final int rows) {
        LOGGER.info("Initializing grid screen");

        if (searchField == null) {
            searchField = new GridSearchBoxWidget(
                font,
                leftPos + 80 + 1,
                topPos + 6 + 1,
                88 - 6,
                new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS),
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            searchField.setX(leftPos + 80 + 1);
            searchField.setY(topPos + 6 + 1);
        }
        getMenu().setSearchBox(searchField);

        getMenu().getView().setListener(this::updateScrollbar);
        updateScrollbar();

        addWidget(searchField);

        if (getMenu().hasProperty(PropertyTypes.REDSTONE_MODE)) {
            addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        }
        addSideButton(new SortingDirectionSideButtonWidget(getMenu()));
        addSideButton(new SortingTypeSideButtonWidget(getMenu()));
        addSideButton(new AutoSelectedSideButtonWidget(getMenu()));
        addSideButton(new ResourceTypeSideButtonWidget(getMenu()));

        final boolean onlyHasNoopSynchronizer = PlatformApi.INSTANCE.getGridSynchronizerRegistry()
            .getAll()
            .stream()
            .allMatch(synchronizer -> synchronizer == NoopGridSynchronizer.INSTANCE);
        if (!onlyHasNoopSynchronizer) {
            addSideButton(new SynchronizationSideButtonWidget(getMenu()));
            searchField.addListener(this::trySynchronizeFromGrid);
        }
    }

    private void trySynchronizeFromGrid(final String text) {
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
        final String text = getMenu().getSynchronizer().getTextToSynchronizeToGrid();
        if (text == null || searchField.getValue().equals(text)) {
            return;
        }
        searchField.setValue(text);
    }

    private void updateScrollbar() {
        this.totalRows = (int) Math.ceil((float) getMenu().getView().getViewList().size() / (float) COLUMNS);
        updateScrollbar(totalRows);
    }

    private boolean isOverStorageArea(final int mouseX, final int mouseY) {
        final int relativeMouseX = mouseX - leftPos;
        final int relativeMouseY = mouseY - topPos;
        return relativeMouseX >= 7
            && relativeMouseX <= 168
            && isInStretchedArea(relativeMouseY);
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphics graphics, final int x, final int y, final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE);
        }
    }

    @Override
    protected int getBottomHeight() {
        return bottomHeight;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    protected void renderRows(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        currentGridSlotIndex = -1;
        for (int row = 0; row < Math.max(totalRows, rows); ++row) {
            final int rowX = x + 7;
            final int rowY = y + topHeight + (row * ROW_SIZE) - getScrollbarOffset();
            final boolean isOutOfFrame = (rowY < y + topHeight - ROW_SIZE)
                || (rowY > y + topHeight + (ROW_SIZE * rows));
            if (isOutOfFrame) {
                continue;
            }
            renderRow(graphics, mouseX, mouseY, rowX, rowY, row);
        }
    }

    private void renderRow(final GuiGraphics graphics,
                           final int mouseX,
                           final int mouseY,
                           final int rowX,
                           final int rowY,
                           final int row) {
        graphics.blit(getTexture(), rowX, rowY, 0, 238, 162, ROW_SIZE);
        for (int column = 0; column < COLUMNS; ++column) {
            renderCell(graphics, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private void renderCell(final GuiGraphics graphics,
                            final int mouseX,
                            final int mouseY,
                            final int rowX,
                            final int rowY,
                            final int idx,
                            final int column) {
        final GridView view = getMenu().getView();
        final int slotX = rowX + 1 + (column * ROW_SIZE);
        final int slotY = rowY + 1;
        if (!getMenu().isActive()) {
            renderDisabledSlot(graphics, slotX, slotY);
        } else {
            renderSlot(graphics, mouseX, mouseY, idx, view, slotX, slotY);
        }
    }

    private void renderSlot(final GuiGraphics graphics,
                            final int mouseX,
                            final int mouseY,
                            final int idx,
                            final GridView view,
                            final int slotX,
                            final int slotY) {
        final boolean inBounds = mouseX >= slotX
            && mouseY >= slotY
            && mouseX <= slotX + 16
            && mouseY <= slotY + 16;
        GridResource resource = null;
        if (idx < view.getViewList().size()) {
            resource = view.getViewList().get(idx);
            renderResourceWithAmount(graphics, slotX, slotY, resource);
        }
        if (inBounds && isOverStorageArea(mouseX, mouseY)) {
            renderSlotHighlight(graphics, slotX, slotY, 0);
            if (resource != null) {
                currentGridSlotIndex = idx;
            }
        }
    }

    private void renderResourceWithAmount(final GuiGraphics graphics,
                                          final int slotX,
                                          final int slotY,
                                          final GridResource resource) {
        if (resource instanceof PlatformGridResource platformResource) {
            platformResource.render(graphics, slotX, slotY);
        }
        renderAmount(graphics, slotX, slotY, resource);
    }

    private void renderAmount(final GuiGraphics graphics,
                              final int slotX,
                              final int slotY,
                              final GridResource resource) {
        if (!(resource instanceof PlatformGridResource platformResource)) {
            return;
        }
        final String text = resource.isZeroed() ? "0" : platformResource.getDisplayedAmount();
        final int color = resource.isZeroed()
            ? requireNonNullElse(ChatFormatting.RED.getColor(), 15)
            : requireNonNullElse(ChatFormatting.WHITE.getColor(), 15);
        final boolean large = (minecraft != null && minecraft.isEnforceUnicode())
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        renderAmount(graphics, slotX, slotY, text, color, large);
    }

    private void renderDisabledSlot(final GuiGraphics graphics, final int slotX, final int slotY) {
        graphics.fillGradient(
            RenderType.guiOverlay(), slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR, DISABLED_SLOT_COLOR, 0
        );
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        super.renderTooltip(graphics, x, y);
        if (isOverStorageArea(x, y)) {
            renderOverStorageAreaTooltip(graphics, x, y);
        }
    }

    private void renderOverStorageAreaTooltip(final GuiGraphics graphics, final int x, final int y) {
        final PlatformGridResource resource = getCurrentGridResource();
        if (resource != null) {
            renderHoveredResourceTooltip(graphics, x, y, menu.getView(), resource);
            return;
        }
        final ItemStack carried = getMenu().getCarried();
        if (carried.isEmpty()) {
            return;
        }
        final List<ClientTooltipComponent> hints = PlatformApi.INSTANCE.getGridInsertionHints().getHints(carried);
        Platform.INSTANCE.renderTooltip(graphics, hints, x, y);
    }

    private void renderHoveredResourceTooltip(final GuiGraphics graphics,
                                              final int mouseX,
                                              final int mouseY,
                                              final GridView view,
                                              final PlatformGridResource platformResource) {
        final ItemStack stackContext = platformResource instanceof ItemGridResource itemGridResource
            ? itemGridResource.getItemStack()
            : ItemStack.EMPTY;
        final List<Component> lines = platformResource.getTooltip();
        final List<ClientTooltipComponent> processedLines = Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            platformResource.getTooltipImage(),
            lines
        );
        if (Platform.INSTANCE.getConfig().getGrid().isDetailedTooltip()) {
            addDetailedTooltip(view, platformResource, processedLines);
        }
        if (!platformResource.isZeroed()) {
            processedLines.addAll(platformResource.getExtractionHints());
        }
        Platform.INSTANCE.renderTooltip(graphics, processedLines, mouseX, mouseY);
    }

    private void addDetailedTooltip(final GridView view,
                                    final PlatformGridResource platformResource,
                                    final List<ClientTooltipComponent> lines) {
        final String amountInTooltip = platformResource.isZeroed() ? "0" : platformResource.getAmountInTooltip();
        lines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", amountInTooltip).withStyle(ChatFormatting.GRAY)
        ));
        platformResource.getTrackedResource(view).ifPresent(entry -> lines.add(new SmallTextClientTooltipComponent(
            getLastModifiedText(entry).withStyle(ChatFormatting.GRAY)
        )));
    }

    private MutableComponent getLastModifiedText(final TrackedResource trackedResource) {
        final LastModified lastModified = LastModified.calculate(trackedResource.getTime(), System.currentTimeMillis());
        if (isModifiedJustNow(lastModified)) {
            return createTranslation("misc", "last_modified.just_now", trackedResource.getSourceName());
        }

        String translationKey = lastModified.type().toString().toLowerCase();
        final boolean plural = lastModified.amount() != 1;
        if (plural) {
            translationKey += "s";
        }

        return createTranslation(
            "misc",
            "last_modified." + translationKey,
            lastModified.amount(),
            trackedResource.getSourceName()
        );
    }

    private boolean isModifiedJustNow(final LastModified lastModified) {
        return lastModified.type() == LastModified.Type.SECOND
            && lastModified.amount() <= MODIFIED_JUST_NOW_MAX_SECONDS;
    }

    @Nullable
    public PlatformGridResource getCurrentGridResource() {
        if (currentGridSlotIndex < 0) {
            return null;
        }
        final List<GridResource> viewList = menu.getView().getViewList();
        if (currentGridSlotIndex >= viewList.size()) {
            return null;
        }
        return (PlatformGridResource) viewList.get(currentGridSlotIndex);
    }

    @Nullable
    public PlatformResourceKey getCurrentResource() {
        final PlatformGridResource resource = getCurrentGridResource();
        return resource != null ? resource.getUnderlyingResource() : null;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (searchField != null) {
            searchField.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        final ItemStack carriedStack = getMenu().getCarried();
        final PlatformGridResource resource = getCurrentGridResource();

        if (resource != null && carriedStack.isEmpty()) {
            mouseClickedInGrid(clickedButton, resource);
            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY)
            && !carriedStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGrid(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private void mouseClickedInGrid(final int clickedButton) {
        final GridInsertMode mode = clickedButton == 1
            ? GridInsertMode.SINGLE_RESOURCE
            : GridInsertMode.ENTIRE_RESOURCE;
        final boolean tryAlternatives = clickedButton == 1;
        getMenu().onInsert(mode, tryAlternatives);
    }

    protected void mouseClickedInGrid(final int clickedButton, final PlatformGridResource resource) {
        resource.onExtract(
            getExtractMode(clickedButton),
            shouldExtractToCursor(),
            getMenu()
        );
    }

    private static GridExtractMode getExtractMode(final int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.HALF_RESOURCE;
        }
        return GridExtractMode.ENTIRE_RESOURCE;
    }

    private static boolean shouldExtractToCursor() {
        return !hasShiftDown();
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean up = delta > 0;

        if (isOverStorageArea((int) x, (int) y)) {
            final PlatformGridResource resource = getCurrentGridResource();
            if (resource != null) {
                mouseScrolledInGrid(up, resource);
            }
        } else if (hoveredSlot != null && hoveredSlot.hasItem() && !(hoveredSlot instanceof DisabledSlot)) {
            mouseScrolledInInventory(up, hoveredSlot);
        }

        return super.mouseScrolled(x, y, z, delta);
    }

    private void mouseScrolledInInventory(final boolean up, final Slot slot) {
        getMenu().getView().setPreventSorting(true);
        final int slotIndex = slot.getContainerSlot();
        mouseScrolledInInventory(up, slot.getItem(), slotIndex);
    }

    private void mouseScrolledInInventory(final boolean up, final ItemStack stack, final int slotIndex) {
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(ItemResource.ofItemStack(stack), scrollMode, slotIndex);
    }

    private void mouseScrolledInGrid(final boolean up, final PlatformGridResource resource) {
        getMenu().getView().setPreventSorting(true);
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        resource.onScroll(scrollMode, getMenu());
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(final boolean up) {
        if (hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(final boolean up) {
        final boolean shift = hasShiftDown();
        final boolean ctrl = hasControlDown();
        if (shift && ctrl) {
            return null;
        }
        return getScrollModeWhenScrollingOnGridArea(up, shift, ctrl);
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(final boolean up,
                                                                       final boolean shift,
                                                                       final boolean ctrl) {
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
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (searchField != null && searchField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        // First do the prevent sorting.
        // Order matters. In auto-selected mode, the search field will swallow the SHIFT key.
        if (hasShiftDown() && Platform.INSTANCE.getConfig().getGrid().isPreventSortingWhileShiftIsDown()) {
            getMenu().getView().setPreventSorting(true);
        }

        if (searchField != null && searchField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(final int key, final int scanCode, final int modifiers) {
        if (getMenu().getView().setPreventSorting(false)) {
            getMenu().getView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}

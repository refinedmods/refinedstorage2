package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridResource;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.History;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighterColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractGridScreen<T extends AbstractGridContainerMenu> extends AbstractBaseScreen<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridScreen.class);

    private static final int MODIFIED_JUST_NOW_MAX_SECONDS = 10;

    private static final int TOP_HEIGHT = 19;
    private static final int INVENTORY_INCLUDING_TITLE_HEIGHT = 99;
    private static final int COLUMNS = 9;
    private static final int MIN_ROWS = 3;

    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final int SELECTION_SLOT_COLOR = -2130706433;

    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    protected final int bottomHeight;
    @Nullable
    protected GridSearchBoxWidget searchField;

    @Nullable
    private ScrollbarWidget scrollbar;
    private int totalRows;
    private int visibleRows;
    private int gridSlotNumber;

    protected AbstractGridScreen(final T menu,
                                 final Inventory playerInventory,
                                 final Component text,
                                 final int bottomHeight) {
        super(menu, playerInventory, text);
        this.bottomHeight = bottomHeight;
        this.menu.setSizeChangedListener(this::init);
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing grid screen");

        this.visibleRows = calculateVisibleRows();
        this.imageHeight = TOP_HEIGHT + (visibleRows * 18) + bottomHeight;
        this.inventoryLabelY = imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 4;

        super.init();

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

        getMenu().addSlots(imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(leftPos + 174, topPos + 20, 12, (visibleRows * 18) - 2);
        this.scrollbar.setScrollAnimation(Platform.INSTANCE.getConfig().getGrid().isSmoothScrolling());
        this.getMenu().getView().setListener(this::resourcesChanged);
        resourcesChanged();

        addWidget(scrollbar);
        addWidget(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new SortingDirectionSideButtonWidget(getMenu()));
        addSideButton(new SortingTypeSideButtonWidget(getMenu()));
        addSideButton(new SizeSideButtonWidget(getMenu()));
        addSideButton(new AutoSelectedSideButtonWidget(getMenu()));
        addSideButton(new StorageChannelTypeSideButtonWidget(getMenu()));

        final PlatformRegistry<GridSynchronizer> registry = PlatformApi.INSTANCE.getGridSynchronizerRegistry();
        if (!registry.isEmpty()) {
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

    private void resourcesChanged() {
        if (scrollbar == null) {
            return;
        }
        totalRows = (int) Math.ceil((float) getMenu().getView().getViewList().size() / (float) COLUMNS);
        scrollbar.setEnabled(totalRows > visibleRows);
        final int rowsExcludingVisibleOnes = totalRows - visibleRows;
        final int maxOffset = scrollbar.isScrollAnimation()
            ? (rowsExcludingVisibleOnes * 18)
            : rowsExcludingVisibleOnes;
        scrollbar.setMaxOffset(maxOffset);
    }

    private int calculateVisibleRows() {
        final int screenSpaceAvailable = height - TOP_HEIGHT - bottomHeight;
        final int maxRows = getMaxRows();
        return Math.max(MIN_ROWS, Math.min((screenSpaceAvailable / 18) - MIN_ROWS, maxRows));
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

    private boolean isOverStorageArea(final int mouseX, final int mouseY) {
        final int relativeMouseX = mouseX - leftPos;
        final int relativeMouseY = mouseY - topPos;
        return relativeMouseX >= 7
            && relativeMouseY >= TOP_HEIGHT
            && relativeMouseX <= 168
            && relativeMouseY <= TOP_HEIGHT + (visibleRows * 18);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        graphics.blit(getTexture(), x, y, 0, 0, imageWidth - 34, TOP_HEIGHT);

        for (int row = 0; row < visibleRows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == visibleRows - 1) {
                textureY = 55;
            }
            graphics.blit(getTexture(), x, y + TOP_HEIGHT + (18 * row), 0, textureY, imageWidth - 34, 18);
        }

        graphics.blit(getTexture(), x, y + TOP_HEIGHT + (18 * visibleRows), 0, 73, imageWidth - 34, bottomHeight);

        gridSlotNumber = -1;

        graphics.enableScissor(x + 7, y + TOP_HEIGHT, x + 7 + (18 * COLUMNS), y + TOP_HEIGHT + (visibleRows * 18));
        for (int row = 0; row < Math.max(totalRows, visibleRows); ++row) {
            renderRow(graphics, mouseX, mouseY, x, y, row);
        }
        graphics.disableScissor();
    }

    private void renderRow(final GuiGraphics graphics,
                           final int mouseX,
                           final int mouseY,
                           final int x,
                           final int y,
                           final int row) {
        final int rowX = x + 7;
        final int rowY = y + TOP_HEIGHT + (row * 18) - getScrollbarOffset();

        final boolean isOutOfFrame = (rowY < y + TOP_HEIGHT - 18) || (rowY > y + TOP_HEIGHT + (visibleRows * 18));
        if (isOutOfFrame) {
            return;
        }

        graphics.blit(getTexture(), rowX, rowY, 0, 238, 162, 18);

        for (int column = 0; column < COLUMNS; ++column) {
            renderCell(graphics, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private int getScrollbarOffset() {
        if (scrollbar == null) {
            return 0;
        }
        final int scrollbarOffset = (int) scrollbar.getOffset();
        if (!scrollbar.isScrollAnimation()) {
            return scrollbarOffset * 18;
        }
        return scrollbarOffset;
    }

    private void renderCell(final GuiGraphics graphics,
                            final int mouseX,
                            final int mouseY,
                            final int rowX,
                            final int rowY,
                            final int idx,
                            final int column) {
        final GridView view = getMenu().getView();

        final int slotX = rowX + 1 + (column * 18);
        final int slotY = rowY + 1;

        GridResource resource = null;
        if (idx < view.getViewList().size()) {
            resource = view.getViewList().get(idx);
            renderResourceWithAmount(graphics, slotX, slotY, resource);
        }

        final boolean inBounds = mouseX >= slotX
            && mouseY >= slotY
            && mouseX <= slotX + 16
            && mouseY <= slotY + 16;

        if (!getMenu().isActive()) {
            renderDisabledSlot(graphics, slotX, slotY);
        } else if (inBounds && isOverStorageArea(mouseX, mouseY)) {
            renderSelection(graphics, slotX, slotY);
            if (resource != null) {
                gridSlotNumber = idx;
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
            ? Objects.requireNonNullElse(ChatFormatting.RED.getColor(), 15)
            : Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15);
        final boolean large = (minecraft != null && minecraft.isEnforceUnicode())
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        renderAmount(graphics, slotX, slotY, text, color, large);
    }

    private void renderDisabledSlot(final GuiGraphics graphics, final int slotX, final int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        graphics.fillGradient(slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR, DISABLED_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderSelection(final GuiGraphics graphics, final int slotX, final int slotY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        graphics.fillGradient(slotX, slotY, slotX + 16, slotY + 16, SELECTION_SLOT_COLOR, SELECTION_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        super.renderTooltip(graphics, x, y);
        if (isOverStorageArea(x, y)) {
            renderOverStorageAreaTooltip(graphics, x, y);
        }
    }

    private void renderOverStorageAreaTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (gridSlotNumber != -1) {
            renderHoveredResourceTooltip(graphics, x, y);
            return;
        }
        final ItemStack carried = getMenu().getCarried();
        if (carried.isEmpty()) {
            return;
        }
        final List<ClientTooltipComponent> hints = PlatformApi.INSTANCE.getGridInsertionHints().getHints(carried);
        Platform.INSTANCE.renderTooltip(graphics, hints, x, y);
    }

    private void renderHoveredResourceTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        final GridView view = getMenu().getView();
        final GridResource resource = view.getViewList().get(gridSlotNumber);
        if (!(resource instanceof PlatformGridResource platformResource)) {
            return;
        }
        renderHoveredResourceTooltip(graphics, mouseX, mouseY, view, platformResource);
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
        final float scale = (minecraft != null && minecraft.isEnforceUnicode()) ? 1F : 0.7F;
        final String amountInTooltip = platformResource.isZeroed() ? "0" : platformResource.getAmountInTooltip();
        lines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", amountInTooltip).withStyle(ChatFormatting.GRAY),
            scale
        ));
        platformResource.getTrackedResource(view).ifPresent(entry -> lines.add(new SmallTextClientTooltipComponent(
            getLastModifiedText(entry).withStyle(ChatFormatting.GRAY),
            scale
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
    public GridResource getHoveredResource() {
        if (this.gridSlotNumber == -1) {
            return null;
        }
        return menu.getView().getViewList().get(this.gridSlotNumber);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (scrollbar != null) {
            scrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (searchField != null) {
            searchField.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (scrollbar != null && scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        final ItemStack carriedStack = getMenu().getCarried();

        if (!getMenu().getView().getViewList().isEmpty() && gridSlotNumber >= 0 && carriedStack.isEmpty()) {
            mouseClickedInGrid(clickedButton, getMenu().getView().getViewList().get(gridSlotNumber));
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

    protected void mouseClickedInGrid(final int clickedButton, final GridResource resource) {
        if (resource instanceof PlatformGridResource platformGridResource) {
            platformGridResource.onExtract(
                getExtractMode(clickedButton),
                shouldExtractToCursor(),
                getMenu()
            );
        }
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
    public void mouseMoved(final double mx, final double my) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        return (scrollbar != null && scrollbar.mouseReleased(mx, my, button)) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double delta) {
        final boolean up = delta > 0;

        if (isOverStorageArea((int) x, (int) y) && gridSlotNumber >= 0) {
            mouseScrolledInGrid(up);
        } else if (hoveredSlot != null && hoveredSlot.hasItem()) {
            mouseScrolledInInventory(up, hoveredSlot);
        }

        final boolean didScrollbar =
            scrollbar != null && !hasShiftDown() && !hasControlDown() && scrollbar.mouseScrolled(x, y, delta);
        return didScrollbar || super.mouseScrolled(x, y, delta);
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
        getMenu().onScroll(
            StorageChannelTypes.ITEM,
            ItemResource.ofItemStack(stack),
            scrollMode,
            slotIndex
        );
    }

    private void mouseScrolledInGrid(final boolean up) {
        getMenu().getView().setPreventSorting(true);
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        final GridResource resource = getMenu().getView().getViewList().get(gridSlotNumber);
        if (!(resource instanceof PlatformGridResource platformGridResource)) {
            return;
        }
        platformGridResource.onScroll(scrollMode, getMenu());
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
        // Order matters. In Auto-selected mode, the search field will swallow the SHIFT key.
        if (hasShiftDown() && Platform.INSTANCE.getConfig().getGrid().isPreventSortingWhileShiftIsDown()) {
            getMenu().getView().setPreventSorting(true);
        }

        if (searchField != null
            && (searchField.keyPressed(key, scanCode, modifiers) || searchField.canConsumeInput())) {
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

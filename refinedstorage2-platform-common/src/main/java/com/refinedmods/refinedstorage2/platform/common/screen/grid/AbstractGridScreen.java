package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.core.History;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.util.LastModified;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.AbstractPlatformGridResource;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.SmallTextTooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage2.query.lexer.SyntaxHighlighterColors;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractGridScreen<R, T extends AbstractGridContainerMenu<R>> extends AbstractBaseScreen<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridScreen.class);

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/grid.png");

    private static final int MODIFIED_JUST_NOW_MAX_SECONDS = 10;

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;
    private static final int COLUMNS = 9;

    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final int SELECTION_SLOT_COLOR = -2130706433;

    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    @Nullable
    private ScrollbarWidget scrollbar;
    @Nullable
    private GridSearchBoxWidget searchField;
    private int totalRows;
    private int visibleRows;
    private int gridSlotNumber;

    protected AbstractGridScreen(final T menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);

        menu.setSizeChangedListener(this::init);

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
            searchField = new GridSearchBoxWidget(
                font,
                leftPos + 80 + 1,
                topPos + 6 + 1,
                88 - 6,
                new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS),
                menu.getView(),
                new GridQueryParserImpl(
                    LexerTokenMappings.DEFAULT_MAPPINGS,
                    ParserOperatorMappings.DEFAULT_MAPPINGS,
                    GridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING
                ),
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            searchField.setX(leftPos + 80 + 1);
            searchField.setY(topPos + 6 + 1);
        }
        getMenu().setSearchBox(searchField);

        getMenu().addSlots(imageHeight - BOTTOM_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(leftPos + 174, topPos + 20, 12, (visibleRows * 18) - 2);
        this.scrollbar.setScrollAnimation(Platform.INSTANCE.getConfig().getGrid().isSmoothScrolling());
        this.getMenu().getView().setListener(this::resourcesChanged);
        resourcesChanged();

        addWidget(scrollbar);
        addWidget(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new SortingDirectionSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new SortingTypeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new SizeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new AutoSelectedSideButtonWidget(getMenu(), this::renderComponentTooltip));

        final OrderedRegistry<ResourceLocation, GridSynchronizer> synchronizerRegistry =
            PlatformApi.INSTANCE.getGridSynchronizerRegistry();
        if (!synchronizerRegistry.isEmpty()) {
            addSideButton(new SynchronizationSideButtonWidget(
                getMenu(),
                this::renderComponentTooltip,
                synchronizerRegistry.getAll()
            ));
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
        totalRows = (int) Math.ceil((float) getMenu().getView().getAll().size() / (float) COLUMNS);
        scrollbar.setEnabled(totalRows > visibleRows);
        final int rowsExcludingVisibleOnes = totalRows - visibleRows;
        final int maxOffset = scrollbar.isScrollAnimation()
            ? (rowsExcludingVisibleOnes * 18)
            : rowsExcludingVisibleOnes;
        scrollbar.setMaxOffset(maxOffset);
    }

    private int calculateVisibleRows() {
        final int screenSpaceAvailable = height - TOP_HEIGHT - BOTTOM_HEIGHT;
        final int maxRows = getMaxRows();
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

    private boolean isOverStorageArea(final int mouseX, final int mouseY) {
        final int relativeMouseX = mouseX - leftPos;
        final int relativeMouseY = mouseY - topPos;
        return relativeMouseX >= 7
            && relativeMouseY >= TOP_HEIGHT
            && relativeMouseX <= 168
            && relativeMouseY <= TOP_HEIGHT + (visibleRows * 18);
    }

    @Override
    protected ResourceLocation getTexture() {
        throw new IllegalStateException("Cannot be called");
    }

    @Override
    protected void renderBg(final PoseStack poseStack, final float delta, final int mouseX, final int mouseY) {
        prepareBackgroundShader(TEXTURE);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

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

    private void renderRow(final PoseStack poseStack,
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

        prepareBackgroundShader(TEXTURE);

        blit(poseStack, rowX, rowY, 0, 238, 162, 18);

        for (int column = 0; column < COLUMNS; ++column) {
            renderColumnInRow(poseStack, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
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

    private void renderColumnInRow(final PoseStack poseStack,
                                   final int mouseX,
                                   final int mouseY,
                                   final int rowX,
                                   final int rowY,
                                   final int idx,
                                   final int column) {
        final GridView view = getMenu().getView();

        final int slotX = rowX + 1 + (column * 18);
        final int slotY = rowY + 1;

        AbstractGridResource resource = null;
        if (idx < view.getAll().size()) {
            resource = view.getAll().get(idx);
            renderResourceWithAmount(poseStack, slotX, slotY, resource);
        }

        final boolean inBounds = mouseX >= slotX
            && mouseY >= slotY
            && mouseX <= slotX + 16
            && mouseY <= slotY + 16;

        if (!getMenu().isActive()) {
            renderDisabledSlot(poseStack, slotX, slotY);
        } else if (inBounds && isOverStorageArea(mouseX, mouseY)) {
            renderSelection(poseStack, slotX, slotY);
            if (resource != null) {
                gridSlotNumber = idx;
            }
        }
    }

    private void renderResourceWithAmount(final PoseStack poseStack,
                                          final int slotX,
                                          final int slotY,
                                          final AbstractGridResource resource) {
        if (resource instanceof AbstractPlatformGridResource platformResource) {
            platformResource.render(poseStack, slotX, slotY);
        }
        renderAmount(poseStack, slotX, slotY, resource);
    }

    private void renderAmount(final PoseStack poseStack,
                              final int slotX,
                              final int slotY,
                              final AbstractGridResource resource) {
        if (!(resource instanceof AbstractPlatformGridResource platformResource)) {
            return;
        }
        final String text = resource.isZeroed() ? "0" : platformResource.getDisplayedAmount();
        final int color = resource.isZeroed()
            ? Objects.requireNonNullElse(ChatFormatting.RED.getColor(), 15)
            : Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15);
        final boolean large = (minecraft != null && minecraft.isEnforceUnicode())
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        renderAmount(poseStack, slotX, slotY, text, color, large);
    }

    private void renderDisabledSlot(final PoseStack poseStack, final int slotX, final int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(poseStack, slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR, DISABLED_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderSelection(final PoseStack poseStack, final int slotX, final int slotY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(poseStack, slotX, slotY, slotX + 16, slotY + 16, SELECTION_SLOT_COLOR, SELECTION_SLOT_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private void renderTooltipWithMaybeSmallLines(final PoseStack poseStack, final int mouseX, final int mouseY) {
        final GridView view = getMenu().getView();
        final AbstractGridResource resource = view.getAll().get(gridSlotNumber);
        if (!(resource instanceof AbstractPlatformGridResource platformResource)) {
            return;
        }

        final List<FormattedCharSequence> lines = platformResource.getTooltip()
            .stream()
            .map(Component::getVisualOrderText)
            .toList();

        if (!Platform.INSTANCE.getConfig().getGrid().isDetailedTooltip()) {
            renderTooltip(poseStack, lines, mouseX, mouseY);
        } else {
            final List<FormattedCharSequence> smallLines = new ArrayList<>();

            final String amountInTooltip = platformResource.isZeroed() ? "0" : platformResource.getAmountInTooltip();
            smallLines.add(createTranslation("misc", "total", amountInTooltip)
                .withStyle(ChatFormatting.GRAY).getVisualOrderText());

            resource.getTrackedResource(view).ifPresent(entry -> smallLines.add(
                getLastModifiedText(entry).withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ));

            SmallTextTooltipRenderer.INSTANCE.render(
                minecraft,
                font,
                poseStack,
                lines,
                smallLines,
                mouseX,
                mouseY,
                width,
                height
            );
        }
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

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (scrollbar != null) {
            scrollbar.render(poseStack, mouseX, mouseY, partialTicks);
        }
        if (searchField != null) {
            searchField.render(poseStack, 0, 0, 0);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (scrollbar != null && scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        final ItemStack carriedStack = getMenu().getCarried();

        if (!getMenu().getView().getAll().isEmpty() && gridSlotNumber >= 0 && carriedStack.isEmpty()) {
            mouseClickedInGrid(clickedButton, getMenu().getView().getAll().get(gridSlotNumber));
            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY)
            && !carriedStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGrid(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    protected abstract void mouseClickedInGrid(int clickedButton);

    protected abstract void mouseClickedInGrid(int clickedButton, AbstractGridResource resource);

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

        return (scrollbar != null && scrollbar.mouseScrolled(x, y, delta)) || super.mouseScrolled(x, y, delta);
    }

    private void mouseScrolledInInventory(final boolean up, final Slot slot) {
        getMenu().getView().setPreventSorting(true);
        final int slotIndex = slot.getContainerSlot();
        mouseScrolledInInventory(up, slot.getItem(), slotIndex);
    }

    protected abstract void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex);

    private void mouseScrolledInGrid(final boolean up) {
        getMenu().getView().setPreventSorting(true);
        final AbstractGridResource resource = getMenu().getView().getAll().get(gridSlotNumber);
        mouseScrolledInGrid(up, resource);
    }

    protected abstract void mouseScrolledInGrid(boolean up, AbstractGridResource resource);

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

package com.refinedmods.refinedstorage.platform.common.support.stretching;

import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.platform.common.support.widget.ScrollbarWidget;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractStretchingScreen<T extends AbstractBaseContainerMenu & ScreenSizeListener>
    extends AbstractBaseScreen<T> {
    protected static final int ROW_SIZE = 18;

    private static final int TOP_HEIGHT = 19;
    private static final int COLUMNS = 9;
    private static final int MIN_ROWS = 3;
    private static final int INVENTORY_INCLUDING_TITLE_HEIGHT = 99;
    private static final int ROW_PADDING = 3;

    private int visibleRows;
    @Nullable
    private ScrollbarWidget scrollbar;

    protected AbstractStretchingScreen(final T menu,
                                       final Inventory playerInventory,
                                       final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        this.visibleRows = calculateVisibleRows();
        this.imageHeight = TOP_HEIGHT + (ROW_SIZE * visibleRows) + getBottomHeight();
        this.inventoryLabelY = imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 4;

        getMenu().onScreenReady(imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 17);

        super.init();

        this.scrollbar = new ScrollbarWidget(leftPos + 174, topPos + 20, 12, (visibleRows * ROW_SIZE) - 2);
        this.scrollbar.setListener(offset -> scrollbarChanged(visibleRows));
        addWidget(scrollbar);

        init(visibleRows);

        addSideButton(new ScreenSizeSideButtonWidget(this));
    }

    protected void init(final int rows) {
        // no op
    }

    protected final int getScrollbarOffset() {
        if (scrollbar == null) {
            return 0;
        }
        final int scrollbarOffset = (int) scrollbar.getOffset();
        if (!scrollbar.isSmoothScrolling()) {
            return scrollbarOffset * ROW_SIZE;
        }
        return scrollbarOffset;
    }

    protected void scrollbarChanged(final int rows) {
        // no op
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        renderBackground(graphics, x, y);
        renderRows(graphics, mouseX, mouseY, x, y);
    }

    private void renderBackground(final GuiGraphics graphics, final int x, final int y) {
        graphics.blit(getTexture(), x, y, 0, 0, imageWidth, TOP_HEIGHT);
        renderStretchingBackground(graphics, x, y + TOP_HEIGHT, visibleRows);
        graphics.blit(
            getTexture(),
            x,
            y + TOP_HEIGHT + (ROW_SIZE * visibleRows),
            0,
            getBottomV(),
            imageWidth,
            getBottomHeight()
        );
    }

    private void renderRows(final GuiGraphics graphics, final int mouseX, final int mouseY, final int x, final int y) {
        graphics.enableScissor(
            x + 7,
            y + TOP_HEIGHT + 1,
            x + 7 + (ROW_SIZE * COLUMNS),
            y + TOP_HEIGHT + 1 + (ROW_SIZE * visibleRows) - 2
        );
        renderRows(graphics, x, y, TOP_HEIGHT, visibleRows, mouseX, mouseY);
        graphics.disableScissor();
    }

    protected abstract void renderRows(
        GuiGraphics graphics,
        int x,
        int y,
        int topHeight,
        int rows,
        int mouseX,
        int mouseY
    );

    protected abstract void renderStretchingBackground(GuiGraphics graphics, int x, int y, int rows);

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (scrollbar != null) {
            scrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (scrollbar != null && scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
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
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didScrollbar = scrollbar != null
            && !hasShiftDown()
            && !hasControlDown()
            && scrollbar.mouseScrolled(x, y, z, delta);
        return didScrollbar || super.mouseScrolled(x, y, z, delta);
    }

    private int calculateVisibleRows() {
        final int screenSpaceAvailable = height - TOP_HEIGHT - getBottomHeight();
        final int maxRows = getMaxRows();
        return Math.max(MIN_ROWS, Math.min((screenSpaceAvailable / ROW_SIZE) - ROW_PADDING, maxRows));
    }

    protected final boolean isInStretchedArea(final int y) {
        return y >= TOP_HEIGHT && y < TOP_HEIGHT + (ROW_SIZE * visibleRows);
    }

    private int getMaxRows() {
        return switch (Platform.INSTANCE.getConfig().getScreenSize()) {
            case STRETCH -> Platform.INSTANCE.getConfig().getMaxRowsStretch();
            case SMALL -> 3;
            case MEDIUM -> 5;
            case LARGE -> 8;
            case EXTRA_LARGE -> 12;
        };
    }

    protected final void updateScrollbar(final int totalRows) {
        if (scrollbar == null) {
            return;
        }
        scrollbar.setEnabled(totalRows > visibleRows);
        final int rowsExcludingVisibleOnes = totalRows - visibleRows;
        final int maxOffset = scrollbar.isSmoothScrolling()
            ? ((rowsExcludingVisibleOnes * ROW_SIZE) + getScrollPanePadding())
            : rowsExcludingVisibleOnes;
        scrollbar.setMaxOffset(maxOffset);
    }

    protected abstract int getBottomHeight();

    protected abstract int getBottomV();

    protected int getScrollPanePadding() {
        return 0;
    }
}

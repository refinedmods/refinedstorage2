package com.refinedmods.refinedstorage.common.support.stretching;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public abstract class AbstractStretchingScreen<T extends AbstractBaseContainerMenu & ScreenSizeListener>
    extends AbstractBaseScreen<T> {
    protected static final int ROW_SIZE = 18;
    protected static final int TOP_HEIGHT = 19;

    private static final int INVENTORY_INCLUDING_TITLE_HEIGHT = 99;
    private static final int COLUMNS = 9;
    private static final int MIN_ROWS = 3;
    private static final int ROW_PADDING = 3;

    private int visibleRows;
    @Nullable
    private ScrollbarWidget scrollbar;

    protected AbstractStretchingScreen(final T menu,
                                       final Inventory playerInventory,
                                       final TextMarquee title,
                                       final int width, final int height) {
        super(menu, playerInventory, title, width, height);
    }

    protected AbstractStretchingScreen(final T menu,
                                       final Inventory playerInventory,
                                       final Component title,
                                       final int width, final int height) {
        super(menu, playerInventory, title, width, height);
    }

    @Override
    protected void init() {
        this.visibleRows = calculateVisibleRows();
        Platform.INSTANCE.updateImageHeight(this, TOP_HEIGHT + (ROW_SIZE * visibleRows) + getBottomHeight());
        this.inventoryLabelY = imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 4;

        resize();

        super.init();

        this.scrollbar = new ScrollbarWidget(
            leftPos + 174,
            topPos + 20,
            ScrollbarWidget.Type.NORMAL,
            (visibleRows * ROW_SIZE) - 2
        );
        this.scrollbar.setListener(offset -> scrollbarChanged(visibleRows));
        addWidget(scrollbar);

        init(visibleRows);

        addSideButton(new ScreenSizeSideButtonWidget(this));
    }

    protected void init(final int rows) {
        // no op
    }

    protected final void resize() {
        getMenu().resized(
            imageHeight - INVENTORY_INCLUDING_TITLE_HEIGHT + 17,
            TOP_HEIGHT + 1,
            TOP_HEIGHT + 1 + (ROW_SIZE * visibleRows) - 2
        );
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
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        renderBackground(graphics, x, y);
        renderRows(graphics, mouseX, mouseY, x, y);
    }

    @Override
    protected void extractDefaultBackground(final GuiGraphicsExtractor graphics) {
        // no op
    }

    private void renderBackground(final GuiGraphicsExtractor graphics, final int x, final int y) {
        graphics.blit(GUI_TEXTURED, getTexture(), x, y, 0, 0, imageWidth, TOP_HEIGHT, 256, 256);
        renderStretchingBackground(graphics, x, y + TOP_HEIGHT, visibleRows);
        graphics.blit(
            GUI_TEXTURED,
            getTexture(),
            x,
            y + TOP_HEIGHT + (ROW_SIZE * visibleRows),
            0,
            getBottomV(),
            imageWidth,
            getBottomHeight(),
            256,
            256
        );
    }

    private void renderRows(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final int x,
                            final int y) {
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
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int topHeight,
        int rows,
        int mouseX,
        int mouseY
    );

    protected abstract void renderStretchingBackground(GuiGraphicsExtractor graphics, int x, int y, int rows);

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (scrollbar != null) {
            scrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (scrollbar != null && scrollbar.mouseClicked(event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (scrollbar != null && scrollbar.mouseReleased(event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didScrollbar = scrollbar != null
            && !minecraft.hasShiftDown()
            && !ClientPlatformUtil.isCommandOrControlDown()
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

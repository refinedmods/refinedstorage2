package com.refinedmods.refinedstorage.common.grid.screen.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorScreen;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class AutocraftingTaskStatusClientTooltipComponent implements ClientTooltipComponent {
    private static final int COLUMNS = 3;
    private static final int ROW_HEIGHT = 30;
    private static final int ROW_WIDTH = 221;
    private static final int STATUS_HEIGHT = 17;
    private static final int STATUS_SPACING = 2;

    private static final Identifier ROW = createIdentifier("autocrafting_monitor/row");

    private final Collection<TaskStatus> statuses;
    private final List<TaskStatus.Item> items;
    private final int rows;

    public AutocraftingTaskStatusClientTooltipComponent(final Collection<TaskStatus> statuses,
                                                        final List<TaskStatus.Item> items) {
        this.statuses = statuses;
        this.items = items;
        this.rows = Math.ceilDiv(items.size(), COLUMNS);
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        int yy = y;
        for (final TaskStatus status : statuses) {
            renderStatus(graphics, x, yy, status);
            yy += STATUS_HEIGHT + STATUS_SPACING;
        }
        for (int i = 0; i < rows; ++i) {
            renderRow(graphics, x, yy, i);
            yy += ROW_HEIGHT;
        }
    }

    private void renderStatus(final GuiGraphicsExtractor graphics, final int x, final int y, final TaskStatus status) {
        final ResourceKey resource = status.info().resource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        renderResourceIcon(graphics, x, y, status, rendering, resource);
        renderStatusText(graphics, x, y, status, rendering, resource);
    }

    private static void renderResourceIcon(final GuiGraphicsExtractor graphics, final int x, final int y,
                                           final TaskStatus status, final ResourceRendering rendering,
                                           final ResourceKey resource) {
        final int resourceX = x + 1;
        rendering.render(resource, graphics, resourceX, y);
        ResourceSlotRendering.renderAmount(graphics, resourceX, y, status.info().amount(), rendering);
    }

    private static void renderStatusText(final GuiGraphicsExtractor graphics, final int x, final int y,
                                         final TaskStatus status, final ResourceRendering rendering,
                                         final ResourceKey resource) {
        final int yOffset = SmallText.isSmall() ? 2 : -1;
        final int textX = x + 3 + 16 + 3;
        final int textY = y + yOffset;
        renderStatusNameAndState(graphics, status, rendering, resource, textX, textY);
        final int ySpacing = SmallText.isSmall() ? 7 : 8;
        renderStatusRunningTime(graphics, status, textX, textY + ySpacing);
    }

    private static void renderStatusNameAndState(final GuiGraphicsExtractor graphics, final TaskStatus status,
                                                 final ResourceRendering rendering, final ResourceKey resource,
                                                 final int textX, final int textY) {
        final Component nameAndState = rendering.getDisplayName(resource)
            .copy().append(" (").append(createTranslation("gui", "autocrafting_monitor.state."
                + status.state().toString().toLowerCase(Locale.ROOT))).append(")");
        SmallText.render(graphics, Minecraft.getInstance().font, nameAndState.getVisualOrderText(),
            textX, textY, 0xFFFFFFFF, true, SmallText.DEFAULT_SCALE);
    }

    private static void renderStatusRunningTime(final GuiGraphicsExtractor graphics, final TaskStatus status,
                                                final int textX,
                                                final int textY) {
        final long percentageCompleted = Math.round(status.percentageCompleted() * 100);
        final String runningTime = getRunningTimeText(status.info().startTime());
        final MutableComponent runningTimeText =
            createTranslation("gui", "autocrafting_monitor.running_time", runningTime)
                .append(" (" + percentageCompleted + "%)");
        SmallText.render(graphics, Minecraft.getInstance().font, runningTimeText.getVisualOrderText(),
            textX, textY, 0xFFFFFFFF, true, SmallText.DEFAULT_SCALE);
    }

    private static String getRunningTimeText(final long startTime) {
        final int totalSecs = (int) (System.currentTimeMillis() - startTime) / 1000;
        final int hours = totalSecs / 3600;
        final int minutes = (totalSecs % 3600) / 60;
        final int seconds = totalSecs % 60;
        final String runningTime;
        if (hours > 0) {
            runningTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            runningTime = String.format("%02d:%02d", minutes, seconds);
        }
        return runningTime;
    }

    private void renderRow(final GuiGraphicsExtractor graphics,
                           final int x,
                           final int y,
                           final int i) {
        graphics.blitSprite(GUI_TEXTURED, ROW, x, y, ROW_WIDTH, ROW_HEIGHT);
        for (int column = i * COLUMNS; column < Math.min(i * COLUMNS + COLUMNS, items.size()); ++column) {
            final TaskStatus.Item item = items.get(column);
            final int xx = x + (column % COLUMNS) * 74;
            AutocraftingMonitorScreen.renderItem(graphics, xx, y, item, false);
        }
    }

    @Override
    public int getHeight(final Font font) {
        return (statuses.size() * (STATUS_HEIGHT + STATUS_SPACING)) + (rows * ROW_HEIGHT);
    }

    @Override
    public int getWidth(final Font font) {
        return ROW_WIDTH;
    }
}

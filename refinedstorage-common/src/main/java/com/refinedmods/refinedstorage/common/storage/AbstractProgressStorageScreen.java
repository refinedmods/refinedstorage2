package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.common.support.widget.ProgressBarWidget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public abstract class AbstractProgressStorageScreen<T extends AbstractStorageContainerMenu & StorageAccessor>
    extends AbstractStorageScreen<T> {
    private final int progressWidgetX;

    @Nullable
    private ProgressBarWidget progressBarWidget;

    protected AbstractProgressStorageScreen(final T menu,
                                            final Inventory inventory,
                                            final Component title,
                                            final int progressWidgetX) {
        super(menu, inventory, title, 176, 223);
        this.inventoryLabelY = 129;
        this.progressWidgetX = progressWidgetX;
    }

    @Override
    protected void init() {
        super.init();
        if (progressBarWidget == null) {
            progressBarWidget = new ProgressBarWidget(
                leftPos + progressWidgetX,
                topPos + 53,
                18,
                72,
                menu::getProgress,
                this::createProgressTooltip
            );
        } else {
            progressBarWidget.setX(leftPos + progressWidgetX);
            progressBarWidget.setY(topPos + 53);
        }
        addRenderableWidget(progressBarWidget);
    }

    private List<Component> createProgressTooltip() {
        final List<Component> tooltip = new ArrayList<>();
        if (menu.hasCapacity()) {
            StorageTooltipHelper.addAmountStoredWithCapacity(
                tooltip::add,
                menu.getStored(),
                menu.getCapacity(),
                this::formatAmount
            );
        } else {
            StorageTooltipHelper.addAmountStoredWithoutCapacity(tooltip::add, menu.getStored(), this::formatAmount);
        }
        return tooltip;
    }
}

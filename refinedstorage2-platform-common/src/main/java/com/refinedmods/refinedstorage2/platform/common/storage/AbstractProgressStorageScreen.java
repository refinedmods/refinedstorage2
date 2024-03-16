package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.common.support.widget.ProgressWidget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractProgressStorageScreen<T extends AbstractStorageContainerMenu & StorageAccessor>
    extends AbstractStorageScreen<T> {
    private final ProgressWidget progressWidget;

    protected AbstractProgressStorageScreen(final T menu,
                                            final Inventory inventory,
                                            final Component title,
                                            final int progressWidgetX) {
        super(menu, inventory, title);
        this.inventoryLabelY = 129;
        this.imageWidth = 176;
        this.imageHeight = 223;
        this.progressWidget = new ProgressWidget(
            progressWidgetX,
            54,
            16,
            70,
            menu::getProgress,
            this::createProgressTooltip
        );
        addRenderableWidget(progressWidget);
    }

    private List<Component> createProgressTooltip() {
        final List<Component> tooltip = new ArrayList<>();
        if (menu.hasCapacity()) {
            StorageTooltipHelper.addAmountStoredWithCapacity(
                tooltip,
                menu.getStored(),
                menu.getCapacity(),
                this::formatQuantity
            );
        } else {
            StorageTooltipHelper.addAmountStoredWithoutCapacity(tooltip, menu.getStored(), this::formatQuantity);
        }
        return tooltip;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        progressWidget.render(graphics, mouseX - leftPos, mouseY - topPos, 0);
    }
}

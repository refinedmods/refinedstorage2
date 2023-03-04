package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageAccessor;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractStorageScreen<T extends AbstractBaseContainerMenu & StorageAccessor>
    extends AbstractBaseScreen<T> {
    private final ProgressWidget progressWidget;
    private final Inventory playerInventory;

    protected AbstractStorageScreen(final T menu,
                                    final Inventory inventory,
                                    final Component title,
                                    final int progressWidgetX) {
        super(menu, inventory, title);

        this.inventoryLabelY = 129;
        this.imageWidth = 176;
        this.imageHeight = 223;
        this.playerInventory = inventory;

        this.progressWidget = new ProgressWidget(
            progressWidgetX,
            54,
            16,
            70,
            menu::getProgress,
            this::renderComponentTooltip,
            this::createTooltip
        );
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void init() {
        super.init();

        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new AccessModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.ACCESS_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new PrioritySideButtonWidget(
            getMenu().getProperty(PropertyTypes.PRIORITY),
            playerInventory,
            this,
            this::renderComponentTooltip
        ));
    }

    private List<Component> createTooltip() {
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

    protected String formatQuantity(final long qty) {
        return AmountFormatting.format(qty);
    }

    @Override
    protected void renderLabels(final PoseStack poseStack, final int mouseX, final int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        progressWidget.render(poseStack, mouseX - leftPos, mouseY - topPos, 0);
    }
}

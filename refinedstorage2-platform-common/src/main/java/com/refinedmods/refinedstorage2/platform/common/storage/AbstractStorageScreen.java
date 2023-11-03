package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.RedstoneModeSideButtonWidget;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractStorageScreen<T extends AbstractStorageContainerMenu & StorageAccessor>
    extends AbstractBaseScreen<T> {
    public static final Component ALLOW_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.allow.help");
    public static final Component BLOCK_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.block.help");

    private static final Component FILTER_MODE_WARNING = createTranslation("gui", "storage.filter_mode.empty_warning");

    private final ProgressWidget progressWidget;
    private final Inventory playerInventory;
    @Nullable
    private FilterModeSideButtonWidget filterModeSideButtonWidget;

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
            this::createTooltip
        );
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void init() {
        super.init();

        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            createTranslation("gui", "storage.redstone_mode_help")
        ));
        filterModeSideButtonWidget = new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            ALLOW_FILTER_MODE_HELP,
            BLOCK_FILTER_MODE_HELP
        );
        addSideButton(filterModeSideButtonWidget);
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.STORAGE
        ));
        addSideButton(new AccessModeSideButtonWidget(getMenu().getProperty(StoragePropertyTypes.ACCESS_MODE)));
        addSideButton(new PrioritySideButtonWidget(
            getMenu().getProperty(StoragePropertyTypes.PRIORITY),
            playerInventory,
            this
        ));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateFilterModeWarning();
    }

    private void updateFilterModeWarning() {
        if (filterModeSideButtonWidget == null) {
            return;
        }
        if (getMenu().shouldDisplayFilterModeWarning()) {
            filterModeSideButtonWidget.setWarning(FILTER_MODE_WARNING);
            return;
        }
        filterModeSideButtonWidget.setWarning(null);
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
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        progressWidget.render(graphics, mouseX - leftPos, mouseY - topPos, 0);
    }
}

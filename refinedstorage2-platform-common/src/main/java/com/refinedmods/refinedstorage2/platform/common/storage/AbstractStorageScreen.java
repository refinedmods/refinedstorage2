package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.RedstoneModeSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractStorageScreen<T extends AbstractStorageContainerMenu> extends AbstractBaseScreen<T> {
    private static final Component ALLOW_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.allow.help");
    private static final Component BLOCK_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.block.help");

    private final Inventory playerInventory;
    @Nullable
    private FilterModeSideButtonWidget filterModeSideButtonWidget;
    @Nullable
    private VoidExcessSideButtonWidget voidExcessSideButtonWidget;

    protected AbstractStorageScreen(final T menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
        this.playerInventory = inventory;
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
        voidExcessSideButtonWidget = new VoidExcessSideButtonWidget(
            getMenu().getProperty(StoragePropertyTypes.VOID_EXCESS)
        );
        addSideButton(voidExcessSideButtonWidget);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (filterModeSideButtonWidget != null) {
            filterModeSideButtonWidget.setWarningVisible(getMenu().shouldDisplayFilterModeWarning());
        }
        if (voidExcessSideButtonWidget != null) {
            voidExcessSideButtonWidget.setWarningVisible(getMenu().shouldDisplayVoidExcessModeWarning());
        }
    }

    protected String formatQuantity(final long qty) {
        return AmountFormatting.format(qty);
    }
}

package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.StoragePrioritySideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

public abstract class AbstractStorageScreen<T extends AbstractStorageContainerMenu> extends AbstractBaseScreen<T> {
    private static final Component ALLOW_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.allow.help");
    private static final Component BLOCK_FILTER_MODE_HELP = createTranslation("gui", "storage.filter_mode.block.help");

    private final Inventory playerInventory;
    @Nullable
    private FilterModeSideButtonWidget filterModeSideButtonWidget;
    @Nullable
    private VoidExcessSideButtonWidget voidExcessSideButtonWidget;

    protected AbstractStorageScreen(final T menu, final Inventory inventory, final Component title,
                                    final int width, final int height) {
        super(menu, inventory, title, width, height);
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
            () -> FuzzyModeSideButtonWidget.Type.STORAGE
        ));
        addSideButton(new AccessModeSideButtonWidget(getMenu().getProperty(StoragePropertyTypes.ACCESS_MODE)));
        addSideButton(new StoragePrioritySideButtonWidget(
            getMenu().getProperty(StoragePropertyTypes.INSERT_PRIORITY),
            getMenu().getProperty(StoragePropertyTypes.EXTRACT_PRIORITY),
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

    protected String formatAmount(final long qty) {
        return format(qty);
    }
}

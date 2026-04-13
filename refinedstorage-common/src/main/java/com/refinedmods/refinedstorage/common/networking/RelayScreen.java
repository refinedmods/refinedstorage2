package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.storage.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.storage.StoragePropertyTypes;
import com.refinedmods.refinedstorage.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.StoragePrioritySideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class RelayScreen extends AbstractFilterScreen<RelayContainerMenu>
    implements RelayContainerMenu.PassThroughListener {
    private static final MutableComponent ALLOW_FILTER_MODE_HELP = createTranslation(
        "gui",
        "relay.filter_mode.allow.help"
    );
    private static final MutableComponent BLOCK_FILTER_MODE_HELP = createTranslation(
        "gui",
        "relay.filter_mode.block.help"
    );

    private final Inventory playerInventory;

    @Nullable
    private AbstractSideButtonWidget passEnergyButton;
    @Nullable
    private AbstractSideButtonWidget passSecurityButton;
    @Nullable
    private AbstractSideButtonWidget passStorageButton;
    @Nullable
    private AbstractSideButtonWidget passAutocraftingButton;
    @Nullable
    private AbstractSideButtonWidget filterModeButton;
    @Nullable
    private AbstractSideButtonWidget fuzzyModeButton;
    @Nullable
    private AbstractSideButtonWidget accessModeButton;
    @Nullable
    private AbstractSideButtonWidget priorityButton;

    public RelayScreen(final RelayContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title, false);
        this.playerInventory = playerInventory;
        menu.setPassThroughListener(this);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RelayPassThroughSideButtonWidget(getMenu().getProperty(RelayPropertyTypes.PASS_THROUGH)));
        final boolean visible = !getMenu().isPassThrough();
        addPassButtons(visible);
        addFilterButtons(visible && (getMenu().isPassStorage() || getMenu().isPassAutocrafting()));
        addStorageButtons(visible && getMenu().isPassStorage());
    }

    private void addPassButtons(final boolean visible) {
        passEnergyButton = new RelayPassEnergySideButtonWidget(getMenu().getProperty(RelayPropertyTypes.PASS_ENERGY));
        passEnergyButton.visible = visible;
        addSideButton(passEnergyButton);

        passSecurityButton = new RelayPassSecuritySideButtonWidget(
            getMenu().getProperty(RelayPropertyTypes.PASS_SECURITY)
        );
        passSecurityButton.visible = visible;
        addSideButton(passSecurityButton);

        passAutocraftingButton = new RelayPassAutocraftingSideButtonWidget(
            getMenu().getProperty(RelayPropertyTypes.PASS_AUTOCRAFTING)
        );
        passAutocraftingButton.visible = visible;
        addSideButton(passAutocraftingButton);

        passStorageButton = new RelayPassStorageSideButtonWidget(
            getMenu().getProperty(RelayPropertyTypes.PASS_STORAGE)
        );
        passStorageButton.visible = visible;
        addSideButton(passStorageButton);
    }

    private void addFilterButtons(final boolean visible) {
        filterModeButton = new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            ALLOW_FILTER_MODE_HELP,
            BLOCK_FILTER_MODE_HELP
        );
        filterModeButton.visible = visible;
        addSideButton(filterModeButton);

        fuzzyModeButton = new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.STORAGE
        );
        fuzzyModeButton.visible = visible;
        addSideButton(fuzzyModeButton);
    }

    private void addStorageButtons(final boolean visible) {
        accessModeButton = new AccessModeSideButtonWidget(getMenu().getProperty(StoragePropertyTypes.ACCESS_MODE));
        accessModeButton.visible = visible;
        addSideButton(accessModeButton);

        priorityButton = new StoragePrioritySideButtonWidget(
            getMenu().getProperty(StoragePropertyTypes.INSERT_PRIORITY),
            getMenu().getProperty(StoragePropertyTypes.EXTRACT_PRIORITY),
            playerInventory,
            this
        );
        priorityButton.visible = visible;
        addSideButton(priorityButton);
    }

    @Override
    public void passThroughChanged(final boolean passThrough,
                                   final boolean passStorage,
                                   final boolean passAutocrafting) {
        updatePassButtons(passThrough);
        updateFilterButtons(!passThrough && (passStorage || passAutocrafting));
        updateStorageButtons(!passThrough && passStorage);
    }

    private void updatePassButtons(final boolean passThrough) {
        if (passEnergyButton != null) {
            passEnergyButton.visible = !passThrough;
        }
        if (passSecurityButton != null) {
            passSecurityButton.visible = !passThrough;
        }
        if (passStorageButton != null) {
            passStorageButton.visible = !passThrough;
        }
        if (passAutocraftingButton != null) {
            passAutocraftingButton.visible = !passThrough;
        }
    }

    private void updateFilterButtons(final boolean visible) {
        if (filterModeButton != null) {
            filterModeButton.visible = visible;
        }
        if (fuzzyModeButton != null) {
            fuzzyModeButton.visible = visible;
        }
    }

    private void updateStorageButtons(final boolean visible) {
        if (accessModeButton != null) {
            accessModeButton.visible = visible;
        }
        if (priorityButton != null) {
            priorityButton.visible = visible;
        }
    }
}

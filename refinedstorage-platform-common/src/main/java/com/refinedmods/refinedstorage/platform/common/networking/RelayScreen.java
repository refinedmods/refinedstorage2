package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.platform.common.storage.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.storage.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.storage.StoragePropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.support.widget.FuzzyModeSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

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
    private AbstractSideButtonWidget filterModeButton;
    @Nullable
    private AbstractSideButtonWidget fuzzyModeButton;
    @Nullable
    private AbstractSideButtonWidget accessModeButton;
    @Nullable
    private AbstractSideButtonWidget priorityButton;

    public RelayScreen(final RelayContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.playerInventory = playerInventory;
        menu.setPassThroughListener(this);
    }

    @Override
    protected boolean hasUpgrades() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RelayPassThroughSideButtonWidget(getMenu().getProperty(RelayPropertyTypes.PASS_THROUGH)));
        final boolean visible = !getMenu().isPassThrough();
        addPassButtons(visible);
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

        passStorageButton = new RelayPassStorageSideButtonWidget(
            getMenu().getProperty(RelayPropertyTypes.PASS_STORAGE)
        );
        passStorageButton.visible = visible;
        addSideButton(passStorageButton);
    }

    private void addStorageButtons(final boolean visible) {
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

        accessModeButton = new AccessModeSideButtonWidget(getMenu().getProperty(StoragePropertyTypes.ACCESS_MODE));
        accessModeButton.visible = visible;
        addSideButton(accessModeButton);

        priorityButton = new PrioritySideButtonWidget(
            getMenu().getProperty(StoragePropertyTypes.PRIORITY),
            playerInventory,
            this
        );
        priorityButton.visible = visible;
        addSideButton(priorityButton);
    }

    @Override
    public void passThroughChanged(final boolean passThrough, final boolean passStorage) {
        updatePassButtons(passThrough);
        updateStorageButtons(passThrough, passStorage);
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
    }

    private void updateStorageButtons(final boolean passThrough, final boolean passStorage) {
        if (filterModeButton != null) {
            filterModeButton.visible = !passThrough && passStorage;
        }
        if (fuzzyModeButton != null) {
            fuzzyModeButton.visible = !passThrough && passStorage;
        }
        if (accessModeButton != null) {
            accessModeButton.visible = !passThrough && passStorage;
        }
        if (priorityButton != null) {
            priorityButton.visible = !passThrough && passStorage;
        }
    }
}

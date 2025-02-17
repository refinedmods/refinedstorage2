package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicatorListener;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicators;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class InterfaceContainerMenu extends AbstractResourceContainerMenu implements ExportingIndicatorListener {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;
    private static final int EXPORT_OUTPUT_SLOT_Y = 66;

    private final ExportingIndicators indicators;

    InterfaceContainerMenu(final int syncId,
                           final Player player,
                           final InterfaceBlockEntity blockEntity,
                           final ResourceContainer exportConfig,
                           final ResourceContainer exportedResources,
                           final Container exportedResourcesAsContainer,
                           final UpgradeContainer upgradeContainer,
                           final ExportingIndicators indicators) {
        super(Menus.INSTANCE.getInterface(), syncId, player);
        addSlots(player, exportConfig, exportedResources, exportedResourcesAsContainer, upgradeContainer);
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            blockEntity::isFuzzyMode,
            blockEntity::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        this.indicators = indicators;
    }

    public InterfaceContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final InterfaceData interfaceData) {
        super(Menus.INSTANCE.getInterface(), syncId);
        final ResourceContainer filterContainer = InterfaceBlockEntity.createFilterContainer(interfaceData);
        final ResourceContainer exportedResources = InterfaceBlockEntity.createExportedResourcesContainer(
            interfaceData,
            FilterWithFuzzyMode.create(filterContainer, null)
        );
        addSlots(playerInventory.player, filterContainer, exportedResources, exportedResources.toItemContainer(),
            new UpgradeContainer(UpgradeDestinations.INTERFACE, 1));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.indicators = new ExportingIndicators(interfaceData.exportingIndicators());
    }

    private void addSlots(final Player player,
                          final ResourceContainer exportConfig,
                          final ResourceContainer exportedResources,
                          final Container exportedResourcesAsContainer,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < exportConfig.size(); ++i) {
            addSlot(createExportConfigSlot(exportConfig, i));
        }
        for (int i = 0; i < exportedResources.size(); ++i) {
            addSlot(addExportedResourceSlot(exportedResources, exportedResourcesAsContainer, i));
        }
        addSlot(new UpgradeSlot(upgradeContainer, 0, 187, 6));
        addPlayerInventory(player.getInventory(), 8, 100);
        transferManager.addBiTransfer(exportedResourcesAsContainer, player.getInventory());
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createExportConfigSlot(
        final ResourceContainer exportConfig,
        final int index
    ) {
        final int x = getExportSlotX(index);
        return new ResourceSlot(
            exportConfig,
            index,
            createTranslation("gui", "interface.filter_help"),
            x,
            EXPORT_CONFIG_SLOT_Y,
            ResourceSlotType.FILTER_WITH_AMOUNT
        );
    }

    private Slot addExportedResourceSlot(
        final ResourceContainer exportedResources,
        final Container exportedResourcesAsContainer,
        final int index
    ) {
        final int x = getExportSlotX(index);
        return new ResourceSlot(
            exportedResources,
            exportedResourcesAsContainer,
            index,
            Component.empty(),
            x,
            EXPORT_OUTPUT_SLOT_Y,
            ResourceSlotType.CONTAINER
        );
    }

    ExportingIndicator getIndicator(final int idx) {
        return indicators.get(idx);
    }

    int getIndicators() {
        return indicators.size();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            indicators.detectChanges(serverPlayer);
        }
    }

    @Override
    public void indicatorChanged(final int index, final ExportingIndicator indicator) {
        indicators.set(index, indicator);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}

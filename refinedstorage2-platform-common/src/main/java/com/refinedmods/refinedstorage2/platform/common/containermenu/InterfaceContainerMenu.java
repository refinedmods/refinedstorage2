package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.OutputSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class InterfaceContainerMenu extends AbstractResourceFilterContainerMenu {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;
    private static final int EXPORT_OUTPUT_SLOT_Y = 66;

    public InterfaceContainerMenu(final int syncId,
                                  final Player player,
                                  final ResourceFilterContainer exportConfig,
                                  final Container exported,
                                  final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getInterface(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            exportConfig
        );
        addSlots(player, exportConfig, exported, upgradeContainer);
    }

    public InterfaceContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getInterface(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());
        addSlots(
            playerInventory.player,
            new FilteredResourceFilterContainer(
                PlatformApi.INSTANCE.getResourceTypeRegistry(),
                9,
                ItemResourceType.INSTANCE,
                64
            ),
            new SimpleContainer(9),
            new UpgradeContainer(UpgradeDestinations.INTERFACE, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        initializeResourceFilterSlots(buf);
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer exportConfig,
                          final Container exportedItems,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < exportConfig.size(); ++i) {
            addSlot(createExportConfigSlot(exportConfig, i));
        }
        for (int i = 0; i < exportedItems.getContainerSize(); ++i) {
            addSlot(createExportOutputSlot(exportedItems, i));
        }
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            addSlot(new Slot(upgradeContainer, i, 187, 6 + (i * 18)));
        }

        addPlayerInventory(player.getInventory(), 8, 100);

        transferManager.addTransfer(exportedItems, player.getInventory());
        transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createExportConfigSlot(final ResourceFilterContainer resourceFilterContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceFilterSlot(resourceFilterContainer, index, x, EXPORT_CONFIG_SLOT_Y);
    }

    private Slot createExportOutputSlot(final Container container, final int index) {
        final int x = getExportSlotX(index);
        return new OutputSlot(container, index, x, EXPORT_OUTPUT_SLOT_Y);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;

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
                                  final Container exported) {
        super(
            Menus.INSTANCE.getInterface(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            exportConfig
        );
        addSlots(player, exportConfig, exported);
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
            new SimpleContainer(9)
        );
        initializeResourceFilterSlots(buf);
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer exportConfig,
                          final Container exportedItems) {
        for (int i = 0; i < exportConfig.size(); ++i) {
            addSlot(createExportConfigSlot(exportConfig, i));
        }
        for (int i = 0; i < exportedItems.getContainerSize(); ++i) {
            addSlot(createExportedItemSlot(exportedItems, i));
        }

        addPlayerInventory(player.getInventory(), 8, 100);

        transferManager.addBiTransfer(exportedItems, player.getInventory());
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createExportConfigSlot(final ResourceFilterContainer resourceFilterContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceFilterSlot(resourceFilterContainer, index, x, EXPORT_CONFIG_SLOT_Y);
    }

    private Slot createExportedItemSlot(final Container container, final int index) {
        final int x = getExportSlotX(index);
        return new Slot(container, index, x, EXPORT_OUTPUT_SLOT_Y);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class InterfaceContainerMenu extends AbstractResourceFilterContainerMenu {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;

    public InterfaceContainerMenu(final int syncId,
                                  final Player player,
                                  final ResourceFilterContainer container,
                                  final UpgradeContainer upgradeContainer) {
        super(Menus.INSTANCE.getInterface(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry(), player, container);
        addSlots(player, container, upgradeContainer);
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
            new UpgradeContainer(1, UpgradeDestinations.INTERFACE, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        initializeResourceFilterSlots(buf);
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer resourceFilterContainer,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < resourceFilterContainer.size(); ++i) {
            addSlot(createExportConfigSlot(resourceFilterContainer, i));
        }
        addSlot(new Slot(upgradeContainer, 0, 187, 6));
        addPlayerInventory(player.getInventory(), 8, 100);

        transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createExportConfigSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
        final int x = EXPORT_CONFIG_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, EXPORT_CONFIG_SLOT_Y);
    }
}

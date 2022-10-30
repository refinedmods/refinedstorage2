package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ExternalStorageContainerMenu extends AbstractStorageContainerMenu implements ResourceTypeAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    public ExternalStorageContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getExternalStorage(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());

        addSlots(
            playerInventory.player,
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9)
        );

        initializeResourceFilterSlots(buf);
    }

    public ExternalStorageContainerMenu(final int syncId,
                                        final Player player,
                                        final ResourceFilterContainer resourceFilterContainer,
                                        final ExternalStorageBlockEntity externalStorage) {
        super(
            Menus.INSTANCE.getExternalStorage(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            externalStorage,
            resourceFilterContainer
        );

        addSlots(player, resourceFilterContainer);
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer resourceFilterContainer) {
        for (int i = 0; i < resourceFilterContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 55);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, FILTER_SLOT_Y);
    }
}

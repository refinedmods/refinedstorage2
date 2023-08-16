package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageContainerMenu extends AbstractStorageContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    public ExternalStorageContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getExternalStorage(), syncId);
        addSlots(playerInventory.player, ResourceContainer.createForFilter());
        initializeResourceSlots(buf);
    }

    public ExternalStorageContainerMenu(final int syncId,
                                        final Player player,
                                        final ResourceContainer resourceContainer,
                                        final StorageConfigurationContainer configContainer) {
        super(Menus.INSTANCE.getExternalStorage(), syncId, player, configContainer);
        addSlots(player, resourceContainer);
    }

    private void addSlots(final Player player,
                          final ResourceContainer resourceContainer) {
        for (int i = 0; i < resourceContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 55);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer resourceContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(
            resourceContainer,
            i,
            createTranslation("gui", "storage.filter_help"),
            x,
            FILTER_SLOT_Y
        );
    }
}

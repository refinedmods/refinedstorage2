package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage.common.storage.StorageConfigurationContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageContainerMenu extends AbstractStorageContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final Predicate<Player> stillValid;

    public ExternalStorageContainerMenu(final int syncId,
                                        final Inventory playerInventory,
                                        final ResourceContainerData resourceContainerData) {
        super(Menus.INSTANCE.getExternalStorage(), syncId);
        addSlots(playerInventory.player, ResourceContainerImpl.createForFilter(resourceContainerData));
        this.stillValid = p -> true;
    }

    ExternalStorageContainerMenu(final int syncId,
                                 final Player player,
                                 final ResourceContainer resourceContainer,
                                 final StorageConfigurationContainer configContainer,
                                 final Predicate<Player> stillValid) {
        super(Menus.INSTANCE.getExternalStorage(), syncId, player, configContainer);
        addSlots(player, resourceContainer);
        this.stillValid = stillValid;
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
            FILTER_SLOT_Y,
            ResourceSlotType.FILTER
        );
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}

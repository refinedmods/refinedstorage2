package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class StorageMonitorContainerMenu extends AbstractResourceContainerMenu {
    private static final Component FILTER_HELP = createTranslation("gui", "storage_monitor.filter_help");

    private final Predicate<Player> stillValid;

    public StorageMonitorContainerMenu(final int syncId,
                                       final Inventory playerInventory,
                                       final ResourceContainerData resourceContainerData) {
        super(Menus.INSTANCE.getStorageMonitor(), syncId);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        addSlots(playerInventory, ResourceContainerImpl.createForFilter(resourceContainerData));
        this.stillValid = p -> true;
    }

    StorageMonitorContainerMenu(final int syncId,
                                final Player player,
                                final StorageMonitorBlockEntity storageMonitor,
                                final ResourceContainer resourceContainer) {
        super(Menus.INSTANCE.getStorageMonitor(), syncId, player);
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            storageMonitor::isFuzzyMode,
            storageMonitor::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            storageMonitor::getRedstoneMode,
            storageMonitor::setRedstoneMode
        ));
        addSlots(player.getInventory(), resourceContainer);
        this.stillValid = p -> Container.stillValidBlockEntity(storageMonitor, p);
    }

    private void addSlots(final Inventory playerInventory, final ResourceContainer resourceContainer) {
        addSlot(new ResourceSlot(resourceContainer, 0, FILTER_HELP, 80, 20, ResourceSlotType.FILTER));
        addPlayerInventory(playerInventory, 8, 55);
        transferManager.addFilterTransfer(playerInventory);
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}

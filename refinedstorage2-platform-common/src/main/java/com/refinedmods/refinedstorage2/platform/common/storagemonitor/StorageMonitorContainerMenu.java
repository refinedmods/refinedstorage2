package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class StorageMonitorContainerMenu extends AbstractResourceContainerMenu {
    private static final Component FILTER_HELP = createTranslation("gui", "storage_monitor.filter_help");

    public StorageMonitorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getStorageMonitor(), syncId);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        addSlots(playerInventory, ResourceContainerImpl.createForFilter(1));
        initializeResourceSlots(buf);
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
    }

    private void addSlots(final Inventory playerInventory, final ResourceContainer resourceContainer) {
        addSlot(new ResourceSlot(resourceContainer, 0, FILTER_HELP, 80, 20, ResourceSlotType.FILTER));
        addPlayerInventory(playerInventory, 8, 55);
        transferManager.addFilterTransfer(playerInventory);
    }
}

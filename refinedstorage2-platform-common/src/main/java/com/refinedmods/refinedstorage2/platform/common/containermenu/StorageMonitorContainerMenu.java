package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storagemonitor.StorageMonitorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

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

    public StorageMonitorContainerMenu(final int syncId,
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
        addSlot(new ResourceSlot(resourceContainer, 0, FILTER_HELP, 80, 20));
        addPlayerInventory(playerInventory, 8, 55);
        transferManager.addFilterTransfer(playerInventory);
    }
}

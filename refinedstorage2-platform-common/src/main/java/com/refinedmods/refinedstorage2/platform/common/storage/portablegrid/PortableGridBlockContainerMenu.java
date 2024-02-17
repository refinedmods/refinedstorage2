package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class PortableGridBlockContainerMenu extends AbstractPortableGridContainerMenu {
    public PortableGridBlockContainerMenu(final int syncId,
                                          final Inventory playerInventory,
                                          final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getPortableGridBlock(), syncId, playerInventory, buf);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        addSlots(0);
    }

    PortableGridBlockContainerMenu(final int syncId,
                                   final Inventory playerInventory,
                                   final AbstractPortableGridBlockEntity portableGrid) {
        super(
            Menus.INSTANCE.getPortableGridBlock(),
            syncId,
            playerInventory,
            portableGrid.getDiskInventory(),
            portableGrid.getGrid(),
            portableGrid.getEnergyStorage()
        );
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            portableGrid::getRedstoneMode,
            portableGrid::setRedstoneMode
        ));
        addSlots(0);
    }
}

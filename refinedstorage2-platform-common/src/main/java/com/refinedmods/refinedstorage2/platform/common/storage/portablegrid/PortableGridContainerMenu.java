package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ValidatedSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class PortableGridContainerMenu extends AbstractGridContainerMenu {
    private final SimpleContainer diskInventory;

    public PortableGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getPortableGrid(), syncId, playerInventory, buf);
        this.diskInventory = new SimpleContainer(1);
        addSlots(0);
    }

    PortableGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final AbstractPortableGridBlockEntity portableGrid) {
        super(Menus.INSTANCE.getPortableGrid(), syncId, playerInventory, portableGrid);
        this.diskInventory = portableGrid.getDiskInventory();
        addSlots(0);
    }

    @Override
    public void addSlots(final int playerInventoryY) {
        super.addSlots(playerInventoryY);
        addSlot(new ValidatedSlot(diskInventory, 0, -19, 8, stack -> stack.getItem() instanceof StorageContainerItem));
        transferManager.addBiTransfer(playerInventory, diskInventory);
    }
}

package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyInfo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class PortableGridContainerMenu extends AbstractGridContainerMenu implements EnergyContainerMenu {
    private final SimpleContainer diskInventory;
    private final EnergyInfo energyInfo;

    public PortableGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getPortableGrid(), syncId, playerInventory, buf);
        this.diskInventory = new SimpleContainer(1);
        this.energyInfo = EnergyInfo.forClient(playerInventory.player, buf.readLong(), buf.readLong());
        addSlots(0);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    PortableGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final AbstractPortableGridBlockEntity portableGrid) {
        super(Menus.INSTANCE.getPortableGrid(), syncId, playerInventory, portableGrid);
        this.diskInventory = portableGrid.getDiskInventory();
        this.energyInfo = EnergyInfo.forServer(
            playerInventory.player,
            portableGrid.getEnergyStorage()::getStored,
            portableGrid.getEnergyStorage()::getCapacity
        );
        addSlots(0);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            portableGrid::getRedstoneMode,
            portableGrid::setRedstoneMode
        ));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        energyInfo.detectChanges();
    }

    @Override
    public void addSlots(final int playerInventoryY) {
        super.addSlots(playerInventoryY);
        addSlot(new ValidatedSlot(diskInventory, 0, -19, 8, stack -> stack.getItem() instanceof StorageContainerItem));
        transferManager.addBiTransfer(playerInventory, diskInventory);
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return energyInfo;
    }
}

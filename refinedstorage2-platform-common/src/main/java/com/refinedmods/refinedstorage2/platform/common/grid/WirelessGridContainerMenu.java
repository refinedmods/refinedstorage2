package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class WirelessGridContainerMenu extends AbstractGridContainerMenu {
    public WirelessGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, buf);
        this.disabledSlot = PlatformApi.INSTANCE.getSlotReference(buf).orElse(null);
        addSlots(0);
    }

    public WirelessGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final Grid grid,
                                     final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, grid);
        this.disabledSlot = slotReference;
        addSlots(0);
    }
}

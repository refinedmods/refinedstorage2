package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class GridContainerMenu extends AbstractGridContainerMenu {
    public GridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf);
        addSlots(0);
    }

    public GridContainerMenu(final int syncId, final Inventory playerInventory, final GridBlockEntity grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid);
        addSlots(0);
    }
}

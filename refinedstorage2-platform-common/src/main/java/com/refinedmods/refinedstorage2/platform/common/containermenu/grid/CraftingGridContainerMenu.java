package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class CraftingGridContainerMenu extends AbstractGridContainerMenu {
    public CraftingGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, buf);
    }

    public CraftingGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final CraftingGridBlockEntity grid) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, grid);
    }
}

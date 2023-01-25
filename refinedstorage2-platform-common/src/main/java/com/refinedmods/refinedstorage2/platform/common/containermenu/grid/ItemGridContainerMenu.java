package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ItemGridContainerMenu extends AbstractGridContainerMenu<ItemResource> {
    public ItemGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf);
    }

    public ItemGridContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final AbstractGridBlockEntity<ItemResource> grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid);
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class FluidGridContainerMenu extends AbstractGridContainerMenu<FluidResource> {
    public FluidGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, buf);
    }

    public FluidGridContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final FluidGridBlockEntity grid) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, grid);
    }
}

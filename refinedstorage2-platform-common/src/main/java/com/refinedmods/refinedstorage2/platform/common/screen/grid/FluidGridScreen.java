package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidGridScreen extends AbstractGridScreen<FluidResource, FluidGridContainerMenu> {
    public FluidGridScreen(final FluidGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }
}

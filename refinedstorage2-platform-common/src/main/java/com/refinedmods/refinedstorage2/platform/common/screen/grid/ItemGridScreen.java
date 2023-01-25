package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemGridScreen extends AbstractGridScreen<ItemResource, ItemGridContainerMenu> {
    public ItemGridScreen(final ItemGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }
}

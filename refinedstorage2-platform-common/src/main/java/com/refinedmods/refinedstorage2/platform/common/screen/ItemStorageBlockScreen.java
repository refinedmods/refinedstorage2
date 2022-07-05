package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.AbstractStorageBlockContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemStorageBlockScreen extends AbstractStorageBlockScreen {
    public ItemStorageBlockScreen(final AbstractStorageBlockContainerMenu menu,
                                  final Inventory inventory,
                                  final Component title) {
        super(menu, inventory, title);
    }
}

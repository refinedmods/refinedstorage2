package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.StorageBlockContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemStorageBlockScreen extends StorageBlockScreen {
    public ItemStorageBlockScreen(final StorageBlockContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }
}

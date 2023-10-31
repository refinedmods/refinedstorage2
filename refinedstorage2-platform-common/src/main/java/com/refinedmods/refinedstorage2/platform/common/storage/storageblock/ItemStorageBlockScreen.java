package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemStorageBlockScreen extends AbstractStorageBlockScreen {
    public ItemStorageBlockScreen(final AbstractStorageBlockContainerMenu menu,
                                  final Inventory inventory,
                                  final Component title) {
        super(menu, inventory, title);
    }
}

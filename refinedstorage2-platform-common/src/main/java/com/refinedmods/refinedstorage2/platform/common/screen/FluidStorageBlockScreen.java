package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.StorageBlockContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidStorageBlockScreen extends StorageBlockScreen {
    public FluidStorageBlockScreen(StorageBlockContainerMenu<?> menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected String format(long qty) {
        return Platform.INSTANCE.getBucketQuantityFormatter().format(qty);
    }
}

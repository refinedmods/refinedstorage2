package com.refinedmods.refinedstorage.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.storage.StorageConfigurationContainer;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class FluidStorageBlockContainerMenu extends AbstractStorageBlockContainerMenu {
    public FluidStorageBlockContainerMenu(final int syncId,
                                          final Inventory playerInventory,
                                          final StorageBlockData storageBlockData) {
        super(
            Menus.INSTANCE.getFluidStorage(),
            syncId,
            playerInventory.player,
            storageBlockData,
            PlatformApi.INSTANCE.getFluidResourceFactory()
        );
    }

    FluidStorageBlockContainerMenu(final int syncId,
                                   final Player player,
                                   final ResourceContainer resourceContainer,
                                   final StorageConfigurationContainer configContainer) {
        super(Menus.INSTANCE.getFluidStorage(), syncId, player, resourceContainer, configContainer);
    }

    @Override
    public boolean hasCapacity() {
        return getCapacity() > 0;
    }
}

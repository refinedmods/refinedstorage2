package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageConfigurationContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ItemStorageBlockContainerMenu extends AbstractStorageBlockContainerMenu {
    public ItemStorageBlockContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getItemStorage(),
            syncId,
            playerInventory.player,
            buf,
            PlatformApi.INSTANCE.getItemResourceFactory()
        );
    }

    ItemStorageBlockContainerMenu(final int syncId,
                                  final Player player,
                                  final ResourceContainer resourceContainer,
                                  final StorageConfigurationContainer configContainer) {
        super(Menus.INSTANCE.getItemStorage(), syncId, player, resourceContainer, configContainer);
    }

    @Override
    public boolean hasCapacity() {
        return getCapacity() > 0;
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ItemStorageBlockContainerMenu extends StorageBlockContainerMenu<ItemResource> {
    public ItemStorageBlockContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getItemStorage(), syncId, playerInventory.player, buf, ItemResourceType.INSTANCE);
    }

    public ItemStorageBlockContainerMenu(int syncId, Player player, ResourceFilterContainer resourceFilterContainer, StorageBlockBlockEntity<?> storageBlock) {
        super(Menus.INSTANCE.getItemStorage(), syncId, player, resourceFilterContainer, storageBlock);
    }
}

package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ItemStorageContainerMenu extends StorageContainerMenu<ItemResource> {
    public ItemStorageContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getItemStorage(), syncId, playerInventory, buf, ItemResourceType.INSTANCE);
    }

    public ItemStorageContainerMenu(int syncId, Player player, ResourceFilterContainer resourceFilterContainer, StorageBlockEntity<?> storageBlock) {
        super(Menus.INSTANCE.getItemStorage(), syncId, player, resourceFilterContainer, storageBlock);
    }
}

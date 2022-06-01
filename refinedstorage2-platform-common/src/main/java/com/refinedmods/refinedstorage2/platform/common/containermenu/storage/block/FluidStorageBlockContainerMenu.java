package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class FluidStorageBlockContainerMenu extends StorageBlockContainerMenu<FluidResource> {
    public FluidStorageBlockContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getFluidStorage(), syncId, playerInventory.player, buf, FluidResourceType.INSTANCE);
    }

    public FluidStorageBlockContainerMenu(int syncId, Player player, ResourceFilterContainer resourceFilterContainer, StorageBlockBlockEntity<?> storageBlock) {
        super(Menus.INSTANCE.getFluidStorage(), syncId, player, resourceFilterContainer, storageBlock);
    }
}

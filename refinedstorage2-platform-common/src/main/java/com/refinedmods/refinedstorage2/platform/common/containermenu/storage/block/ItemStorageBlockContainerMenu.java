package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;

import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ItemStorageBlockContainerMenu extends StorageBlockContainerMenu<ItemResource> {
    private static final Set<StorageTooltipHelper.TooltipOption> TOOLTIP_OPTIONS = Set.of(
            StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS,
            StorageTooltipHelper.TooltipOption.STACK_INFO
    );
    private static final Set<StorageTooltipHelper.TooltipOption> TOOLTIP_OPTIONS_WITH_ONLY_STACK_INFO = Set.of(
            StorageTooltipHelper.TooltipOption.STACK_INFO
    );

    public ItemStorageBlockContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getItemStorage(), syncId, playerInventory.player, buf, ItemResourceType.INSTANCE);
    }

    public ItemStorageBlockContainerMenu(int syncId, Player player, ResourceFilterContainer resourceFilterContainer, StorageBlockBlockEntity<?> storageBlock) {
        super(Menus.INSTANCE.getItemStorage(), syncId, player, resourceFilterContainer, storageBlock);
    }

    @Override
    public Set<StorageTooltipHelper.TooltipOption> getTooltipOptions() {
        return getCapacity() > 0 ? TOOLTIP_OPTIONS : TOOLTIP_OPTIONS_WITH_ONLY_STACK_INFO;
    }
}

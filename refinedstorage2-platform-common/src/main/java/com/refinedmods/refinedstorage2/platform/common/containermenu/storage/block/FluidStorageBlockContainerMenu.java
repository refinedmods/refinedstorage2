package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.AbstractStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import java.util.Collections;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class FluidStorageBlockContainerMenu extends AbstractStorageBlockContainerMenu {
    private static final Set<StorageTooltipHelper.TooltipOption> TOOLTIP_OPTIONS =
        Set.of(StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS);

    public FluidStorageBlockContainerMenu(final int syncId,
                                          final Inventory playerInventory,
                                          final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getFluidStorage(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            playerInventory.player,
            buf,
            FluidResourceType.INSTANCE
        );
    }

    public FluidStorageBlockContainerMenu(final int syncId,
                                          final Player player,
                                          final ResourceFilterContainer resourceFilterContainer,
                                          final AbstractStorageBlockBlockEntity<?> storageBlock) {
        super(
            Menus.INSTANCE.getFluidStorage(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            resourceFilterContainer,
            storageBlock
        );
    }

    @Override
    public Set<StorageTooltipHelper.TooltipOption> getTooltipOptions() {
        return getCapacity() > 0 ? TOOLTIP_OPTIONS : Collections.emptySet();
    }
}

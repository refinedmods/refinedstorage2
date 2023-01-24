package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.world.entity.player.Player;

public abstract class AbstractItemGridInsertionStrategy implements GridInsertionStrategy {
    protected final GridService<ItemResource> createGridService(final Player player,
                                                                final GridServiceFactory gridServiceFactory) {
        return gridServiceFactory.create(
            StorageChannelTypes.ITEM,
            new PlayerActor(player),
            this::getMaxStackSize,
            1
        );
    }

    protected final long getMaxStackSize(final ItemResource itemResource) {
        return itemResource.item().getMaxStackSize();
    }
}

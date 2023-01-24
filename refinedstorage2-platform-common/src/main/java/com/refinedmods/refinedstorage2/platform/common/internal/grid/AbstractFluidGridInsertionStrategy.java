package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.world.entity.player.Player;

public abstract class AbstractFluidGridInsertionStrategy implements GridInsertionStrategy {
    protected final GridService<FluidResource> createGridService(final Player player,
                                                                 final GridServiceFactory gridServiceFactory) {
        return gridServiceFactory.create(
            StorageChannelTypes.FLUID,
            new PlayerActor(player),
            resource -> Long.MAX_VALUE,
            Platform.INSTANCE.getBucketAmount()
        );
    }
}

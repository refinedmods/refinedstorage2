package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.BucketPlayerInventoryInsertableStorage;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.minecraft.world.entity.player.Player;

public class FluidStorageMonitorExtractionStrategy implements StorageMonitorExtractionStrategy {
    @Override
    public boolean extract(final Object resource,
                           final boolean fullStack,
                           final Player player,
                           final Actor actor,
                           final Network network) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return false;
        }
        final BucketPlayerInventoryInsertableStorage target = new BucketPlayerInventoryInsertableStorage(
            player.getInventory(),
            network.getComponent(StorageNetworkComponent.class).getStorageChannel(StorageChannelTypes.ITEM),
            true
        );
        final StorageChannel<FluidResource> source = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.FLUID);
        return TransferHelper.transfer(
            fluidResource,
            Platform.INSTANCE.getBucketAmount(),
            actor,
            source,
            target,
            source
        ) > 0;
    }
}

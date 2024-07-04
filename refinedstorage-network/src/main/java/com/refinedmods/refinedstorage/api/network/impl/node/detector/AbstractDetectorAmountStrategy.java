package com.refinedmods.refinedstorage.api.network.impl.node.detector;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

public abstract class AbstractDetectorAmountStrategy implements DetectorAmountStrategy {
    protected StorageChannel getStorageChannel(final Network network) {
        return network.getComponent(StorageNetworkComponent.class);
    }
}

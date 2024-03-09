package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

public class DetectorAmountStrategyImpl extends AbstractDetectorAmountStrategy {
    @Override
    public long getAmount(final Network network, final ResourceKey configuredResource) {
        return getStorageChannel(network)
            .get(configuredResource)
            .map(ResourceAmount::getAmount)
            .orElse(0L);
    }
}

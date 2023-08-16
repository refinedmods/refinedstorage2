package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;

public class DetectorAmountStrategyImpl extends AbstractDetectorAmountStrategy {
    @Override
    public <T> long getAmount(final Network network, final ResourceTemplate<T> template) {
        return getStorageChannel(network, template)
            .get(template.resource())
            .map(ResourceAmount::getAmount)
            .orElse(0L);
    }
}

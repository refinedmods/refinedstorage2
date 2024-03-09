package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

public interface DetectorAmountStrategy {
    long getAmount(Network network, ResourceKey configuredResource);
}

package com.refinedmods.refinedstorage.api.network.impl.node.detector;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

public interface DetectorAmountStrategy {
    long getAmount(Network network, ResourceKey configuredResource);
}

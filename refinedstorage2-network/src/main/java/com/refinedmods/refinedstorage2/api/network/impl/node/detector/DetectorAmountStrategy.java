package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;

public interface DetectorAmountStrategy {
    long getAmount(Network network, ResourceTemplate template);
}

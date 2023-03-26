package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class DetectorNetworkNodeFactory extends AbstractNetworkNodeFactory<DetectorNetworkNode> {
    @Override
    protected DetectorNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new DetectorNetworkNode(getEnergyUsage(properties));
    }
}

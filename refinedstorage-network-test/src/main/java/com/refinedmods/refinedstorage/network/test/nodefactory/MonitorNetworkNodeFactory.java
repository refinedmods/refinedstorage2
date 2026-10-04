package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNetworkNode;

import java.util.Map;

public class MonitorNetworkNodeFactory extends AbstractNetworkNodeFactory {
    @Override
    protected AbstractNetworkNode innerCreate(final Map<String, Object> properties) {
        return new MonitorNetworkNode(getEnergyUsage(properties));
    }
}

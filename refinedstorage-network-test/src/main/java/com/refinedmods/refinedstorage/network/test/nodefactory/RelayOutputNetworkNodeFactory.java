package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;

import java.util.Map;

public class RelayOutputNetworkNodeFactory extends AbstractNetworkNodeFactory {
    @Override
    protected AbstractNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new RelayOutputNetworkNode(getEnergyUsage(properties));
    }
}

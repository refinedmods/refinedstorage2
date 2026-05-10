package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;

import java.util.Map;

public class RelayOutputNetworkNodeFactory extends AbstractNetworkNodeFactory {
    @Override
    protected AbstractNetworkNode innerCreate(final Map<String, Object> properties) {
        final RelayOutputNetworkNode node = new RelayOutputNetworkNode(getEnergyUsage(properties));
        final ExternalPatternSinkKey key = new ExternalPatternSinkKey() {
        };
        node.setSinkKeyProvider(() -> key);
        return node;
    }
}

package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;

import java.util.Map;

public class PatternProviderNetworkNodeFactory extends AbstractNetworkNodeFactory {
    public static final String PROPERTY_SIZE = "size";

    @Override
    protected AbstractNetworkNode innerCreate(final Map<String, Object> properties) {
        final int size = (int) properties.getOrDefault(PROPERTY_SIZE, 9);
        final PatternProviderNetworkNode node = new PatternProviderNetworkNode(getEnergyUsage(properties), size);
        final ExternalPatternSinkKey key = new ExternalPatternSinkKey() {
        };
        node.setSinkKeyProvider(() -> key);
        return node;
    }
}

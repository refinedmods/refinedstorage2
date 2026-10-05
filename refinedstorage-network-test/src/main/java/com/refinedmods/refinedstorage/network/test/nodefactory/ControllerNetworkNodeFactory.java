package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;

import java.util.Map;

public class ControllerNetworkNodeFactory extends AbstractNetworkNodeFactory {
    public static final NetworkNodeType TYPE = new NetworkNodeType() {
    };

    @Override
    protected AbstractNetworkNode innerCreate(final Map<String, Object> properties) {
        return new ControllerNetworkNode(TYPE);
    }
}

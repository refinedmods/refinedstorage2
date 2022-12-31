package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class ControllerNetworkNodeFactory extends AbstractNetworkNodeFactory<ControllerNetworkNode> {
    @Override
    protected ControllerNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new ControllerNetworkNode();
    }
}

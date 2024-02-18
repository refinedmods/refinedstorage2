package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class ImporterNetworkNodeFactory extends AbstractNetworkNodeFactory {
    @Override
    protected AbstractNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new ImporterNetworkNode(getEnergyUsage(properties));
    }
}

package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class ImporterNetworkNodeFactory extends AbstractNetworkNodeFactory<ImporterNetworkNode> {
    @Override
    protected ImporterNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new ImporterNetworkNode(getEnergyUsage(properties));
    }
}

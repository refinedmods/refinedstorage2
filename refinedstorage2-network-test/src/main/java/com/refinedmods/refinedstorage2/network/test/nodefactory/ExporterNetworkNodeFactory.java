package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.FirstAvailableExporterSchedulingMode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class ExporterNetworkNodeFactory extends AbstractNetworkNodeFactory<ExporterNetworkNode> {
    @Override
    protected ExporterNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new ExporterNetworkNode(
            getEnergyUsage(properties),
            FirstAvailableExporterSchedulingMode.INSTANCE
        );
    }
}

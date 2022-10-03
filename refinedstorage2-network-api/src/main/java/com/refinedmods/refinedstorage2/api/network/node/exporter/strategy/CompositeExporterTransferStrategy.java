package com.refinedmods.refinedstorage2.api.network.node.exporter.strategy;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class CompositeExporterTransferStrategy implements ExporterTransferStrategy {
    private final List<ExporterTransferStrategy> strategies;

    public CompositeExporterTransferStrategy(final List<ExporterTransferStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean transfer(final Object resource, final Actor actor, final Network network) {
        for (final ExporterTransferStrategy strategy : strategies) {
            if (strategy.transfer(resource, actor, network)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeExporterTransferStrategy{"
            + "strategies=" + strategies
            + '}';
    }
}

package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class FirstAvailableExporterTransferStrategyExecutor implements ExporterTransferStrategyExecutor {
    public static final ExporterTransferStrategyExecutor INSTANCE =
        new FirstAvailableExporterTransferStrategyExecutor();

    private FirstAvailableExporterTransferStrategyExecutor() {
    }

    @Override
    public void execute(final List<ExporterTransferStrategy> strategies, final Network network, final Actor actor) {
        for (final ExporterTransferStrategy strategy : strategies) {
            if (strategy.transfer(actor, network)) {
                break;
            }
        }
    }
}

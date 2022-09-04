package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class RoundRobinExporterTransferStrategyExecutor implements ExporterTransferStrategyExecutor {
    private int index;

    @Override
    public void execute(final List<ExporterTransferStrategy> strategies, final Network network, final Actor actor) {
        final ExporterTransferStrategy strategy = strategies.get(index % strategies.size());
        strategy.transfer(actor, network);
        index = (index + 1) % strategies.size();
    }

    @Override
    public void onTemplatesChanged() {
        index = 0;
    }
}

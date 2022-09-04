package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class RandomExporterTransferStrategyExecutor implements ExporterTransferStrategyExecutor {
    private final Randomizer randomizer;

    public RandomExporterTransferStrategyExecutor(final Randomizer randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public void execute(final List<ExporterTransferStrategy> strategies, final Network network, final Actor actor) {
        randomizer.choose(strategies).transfer(actor, network);
    }
}

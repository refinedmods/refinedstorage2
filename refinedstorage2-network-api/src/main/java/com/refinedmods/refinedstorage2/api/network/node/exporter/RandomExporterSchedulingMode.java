package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class RandomExporterSchedulingMode implements ExporterSchedulingMode {
    private final Randomizer randomizer;

    public RandomExporterSchedulingMode(final Randomizer randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public void execute(final List<Object> templates,
                        final ExporterTransferStrategy strategy,
                        final Network network,
                        final Actor actor) {
        if (templates.isEmpty()) {
            return;
        }
        final Object template = randomizer.choose(templates);
        strategy.transfer(template, actor, network);
    }
}

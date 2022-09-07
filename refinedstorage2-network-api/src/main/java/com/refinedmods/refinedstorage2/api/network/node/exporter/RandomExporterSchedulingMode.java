package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
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
        final List<Object> shuffledTemplates = new ArrayList<>(templates);
        randomizer.shuffle(shuffledTemplates);
        executeFirstSuccessful(strategy, network, actor, shuffledTemplates);
    }

    private static void executeFirstSuccessful(final ExporterTransferStrategy strategy,
                                               final Network network,
                                               final Actor actor,
                                               final List<Object> templates) {
        for (final Object template : templates) {
            if (strategy.transfer(template, actor, network)) {
                return;
            }
        }
    }
}

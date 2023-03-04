package com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling.ExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
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
        FirstAvailableExporterSchedulingMode.INSTANCE.execute(shuffledTemplates, strategy, network, actor);
    }

    public interface Randomizer {
        <T> void shuffle(List<T> list);
    }
}

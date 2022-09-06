package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class FirstAvailableExporterSchedulingMode implements ExporterSchedulingMode {
    public static final ExporterSchedulingMode INSTANCE =
        new FirstAvailableExporterSchedulingMode();

    private FirstAvailableExporterSchedulingMode() {
    }

    @Override
    public void execute(final List<Object> templates,
                        final ExporterTransferStrategy strategy,
                        final Network network,
                        final Actor actor) {
        for (final Object template : templates) {
            if (strategy.transfer(template, actor, network)) {
                break;
            }
        }
    }
}

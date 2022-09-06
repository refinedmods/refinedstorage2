package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class RoundRobinExporterSchedulingMode implements ExporterSchedulingMode {
    private int index;

    @Override
    public void execute(final List<Object> templates,
                        final ExporterTransferStrategy strategy,
                        final Network network,
                        final Actor actor) {
        if (templates.isEmpty()) {
            return;
        }
        final Object template = templates.get(index % templates.size());
        strategy.transfer(template, actor, network);
        index = (index + 1) % templates.size();
    }

    @Override
    public void onTemplatesChanged() {
        index = 0;
    }
}

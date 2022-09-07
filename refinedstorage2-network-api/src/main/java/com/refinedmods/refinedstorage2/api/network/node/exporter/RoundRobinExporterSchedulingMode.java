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
        final int startIndex = index % templates.size();
        for (int i = startIndex; i < templates.size(); ++i) {
            final Object template = templates.get(i);
            if (strategy.transfer(template, actor, network)) {
                index = (index + 1) % templates.size();
                return;
            }
        }
        index = 0;
    }

    @Override
    public void onTemplatesChanged() {
        index = 0;
    }
}

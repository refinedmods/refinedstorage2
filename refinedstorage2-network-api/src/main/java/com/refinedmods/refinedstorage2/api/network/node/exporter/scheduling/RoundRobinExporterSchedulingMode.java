package com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

public class RoundRobinExporterSchedulingMode implements ExporterSchedulingMode {
    private final RoundRobinState state;

    public RoundRobinExporterSchedulingMode(final RoundRobinState state) {
        this.state = state;
    }

    @Override
    public void execute(final List<Object> templates,
                        final ExporterTransferStrategy strategy,
                        final Network network,
                        final Actor actor) {
        if (templates.isEmpty()) {
            return;
        }
        final int startIndex = state.getIndex() % templates.size();
        for (int i = startIndex; i < templates.size(); ++i) {
            final Object template = templates.get(i);
            if (strategy.transfer(template, actor, network)) {
                state.setIndex((state.getIndex() + 1) % templates.size());
                return;
            }
        }
        state.setIndex(0);
    }

    public RoundRobinState getState() {
        return state;
    }
}

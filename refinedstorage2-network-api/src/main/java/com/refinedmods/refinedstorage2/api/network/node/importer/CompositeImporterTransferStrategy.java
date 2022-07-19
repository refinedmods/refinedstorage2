package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.Collections;
import java.util.List;

public class CompositeImporterTransferStrategy implements ImporterTransferStrategy {
    private final List<ImporterTransferStrategy> strategies;

    public CompositeImporterTransferStrategy(final List<ImporterTransferStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean transfer(final Filter filter, final Actor actor, final Network network) {
        for (final ImporterTransferStrategy strategy : strategies) {
            if (strategy.transfer(filter, actor, network)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeImporterTransferStrategy{"
            + "strategies=" + strategies
            + '}';
    }
}

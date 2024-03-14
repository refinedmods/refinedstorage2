package com.refinedmods.refinedstorage2.api.network.impl.node.importer;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class ImporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Filter filter = new Filter();
    private final Actor actor = new NetworkNodeActor(this);
    private final List<ImporterTransferStrategy> transferStrategies = new ArrayList<>();

    public ImporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategies(final List<ImporterTransferStrategy> transferStrategies) {
        this.transferStrategies.clear();
        this.transferStrategies.addAll(transferStrategies);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive()) {
            return;
        }
        for (final ImporterTransferStrategy transferStrategy : transferStrategies) {
            if (transferStrategy.transfer(filter, actor, network)) {
                return;
            }
        }
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
    }

    public void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        filter.setNormalizer(normalizer);
    }

    public void setFilters(final Set<ResourceKey> filters) {
        filter.setFilters(filters);
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

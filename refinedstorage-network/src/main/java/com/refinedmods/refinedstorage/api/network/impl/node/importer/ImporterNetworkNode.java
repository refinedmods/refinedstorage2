package com.refinedmods.refinedstorage.api.network.impl.node.importer;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeEventManager;
import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeTypes;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Set;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

public class ImporterNetworkNode extends AbstractNetworkNode implements NetworkNodeDetailsProvider {
    private final Filter filter = new Filter();
    private final Actor actor = new NetworkNodeActor(this);
    private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();
    private long energyUsage;
    @Nullable
    private ImporterTransferStrategy transferStrategy;

    public ImporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategy(final ImporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || transferStrategy == null) {
            return;
        }
        transferStrategy.transfer(filter, actor, network);
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

    @Override
    public NetworkNodeType getType() {
        return NetworkNodeTypes.IMPORTER;
    }

    @Override
    public NetworkNodeDetails createDetails() {
        return new SimpleNetworkNodeDetails(getEnergyUsage(), isActive());
    }

    @Override
    public void addListener(final NetworkNodeListener listener) {
        eventManager.addListener(listener);
    }

    @Override
    public void removeListener(final NetworkNodeListener listener) {
        eventManager.removeListener(listener);
    }
}

package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ExporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    private final List<ExporterTransferStrategy> strategies = new ArrayList<>();
    @Nullable
    private ExporterTransferStrategyFactory strategyFactory;

    public ExporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setStrategyFactory(@Nullable final ExporterTransferStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive()) {
            return;
        }
        for (final ExporterTransferStrategy strategy : strategies) {
            if (strategy.transfer(actor, network)) {
                break;
            }
        }
    }

    public void setTemplates(final List<Object> newTemplates) {
        strategies.clear();
        if (strategyFactory == null) {
            return;
        }
        strategies.addAll(newTemplates.stream().flatMap(t -> strategyFactory.create(t).stream()).toList());
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

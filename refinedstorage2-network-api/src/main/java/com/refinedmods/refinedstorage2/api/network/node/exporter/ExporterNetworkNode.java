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
    private ExporterTransferStrategyExecutor strategyExecutor;

    public ExporterNetworkNode(final long energyUsage, final ExporterTransferStrategyExecutor strategyExecutor) {
        this.energyUsage = energyUsage;
        this.strategyExecutor = strategyExecutor;
    }

    public void setStrategyFactory(@Nullable final ExporterTransferStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void setStrategyExecutor(final ExporterTransferStrategyExecutor strategyExecutor) {
        this.strategyExecutor = strategyExecutor;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || strategies.isEmpty()) {
            return;
        }
        strategyExecutor.execute(strategies, network, actor);
    }

    public void setTemplates(final List<Object> newTemplates) {
        strategies.clear();
        strategyExecutor.onTemplatesChanged();
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

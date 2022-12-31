package com.refinedmods.refinedstorage2.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling.ExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

// TODO: Move to own module (refinedstorage2-network), everything in api should have @Api annot.
public class ExporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    private final List<Object> templates = new ArrayList<>();
    @Nullable
    private ExporterTransferStrategy transferStrategy;
    @Nullable
    private ExporterSchedulingMode schedulingMode;

    public ExporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategy(@Nullable final ExporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    public void setSchedulingMode(@Nullable final ExporterSchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || transferStrategy == null || schedulingMode == null) {
            return;
        }
        schedulingMode.execute(templates, transferStrategy, network, actor);
    }

    public void setTemplates(final List<Object> newTemplates) {
        templates.clear();
        templates.addAll(newTemplates);
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

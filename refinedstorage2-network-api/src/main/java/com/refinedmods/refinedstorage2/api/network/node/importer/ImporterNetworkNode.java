package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

// TODO: Write a gametest.
public class ImporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Filter filter = new Filter();
    private final Actor actor = new NetworkNodeActor(this);
    private long coolDownTime;
    private long coolDownTimer;

    @Nullable
    private ImporterTransferStrategy transferStrategy;

    public ImporterNetworkNode(final long energyUsage, final long coolDownTime) {
        this.energyUsage = energyUsage;
        this.coolDownTime = coolDownTime;
    }

    public void setTransferStrategy(@Nullable final ImporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    @Override
    public void doWork() {
        if (isActive() && transferStrategy != null) {
            tryTransfer(transferStrategy);
        }
    }

    private void tryTransfer(final ImporterTransferStrategy strategy) {
        // TODO: Remove timer from this crap.
        --coolDownTimer;
        if (coolDownTimer < 0) {
            strategy.transfer(filter, actor, Objects.requireNonNull(network));
            coolDownTimer = coolDownTime;
        }
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
    }

    public void setNormalizer(final UnaryOperator<Object> normalizer) {
        filter.setNormalizer(normalizer);
    }

    public void setFilterTemplates(final Set<Object> templates) {
        filter.setTemplates(templates);
    }

    public void setCoolDownTime(final long coolDownTime) {
        this.coolDownTime = coolDownTime;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

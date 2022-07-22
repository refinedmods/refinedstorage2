package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

// TODO: forge source impl.
// TODO: fabric fluids impl. (test it)
// TODO: no disk drive import?! (TEST IT, network IT test)
// TODO: gui config etc.
public class ImporterNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private final Filter filter = new Filter();
    private final Actor actor = new NetworkNodeActor(this);
    private final long coolDownTime;
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
    public void update() {
        super.update();
        if (transferStrategy != null && isActive()) {
            tryTransfer(transferStrategy);
        }
    }

    private void tryTransfer(final ImporterTransferStrategy strategy) {
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

    public void setFilterTemplates(final Set<Object> templates) {
        filter.setTemplates(templates);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

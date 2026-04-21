package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class WirelessAutocraftingMonitor implements AutocraftingMonitor {
    private final NetworkItemContext context;

    WirelessAutocraftingMonitor(final NetworkItemContext context) {
        this.context = context;
    }

    private Optional<AutocraftingNetworkComponent> getAutocrafting() {
        return context.resolveNetwork().map(network -> network.getComponent(AutocraftingNetworkComponent.class));
    }

    @Override
    public void addWatcher(final AutocraftingMonitorWatcher watcher) {
        context.drainEnergy(Platform.INSTANCE.getConfig().getWirelessAutocraftingMonitor().getOpenEnergyUsage());
    }

    @Override
    public void removeWatcher(final AutocraftingMonitorWatcher watcher) {
        // no op
    }

    @Override
    public boolean isAutocraftingMonitorActive() {
        final boolean networkActive = context.resolveNetwork()
            .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
                || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
        return networkActive && context.isActive();
    }

    @Override
    public List<TaskStatus> getStatuses() {
        return getAutocrafting().map(AutocraftingNetworkComponent::getStatuses).orElse(Collections.emptyList());
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        getAutocrafting().ifPresent(autocrafting -> autocrafting.addListener(listener));
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        getAutocrafting().ifPresent(autocrafting -> autocrafting.removeListener(listener));
    }

    @Override
    public void cancel(final TaskId taskId) {
        getAutocrafting().ifPresent(autocrafting -> {
            autocrafting.cancel(taskId);
            context.drainEnergy(Platform.INSTANCE.getConfig().getWirelessAutocraftingMonitor().getCancelEnergyUsage());
        });
    }

    @Override
    public void cancelAll() {
        getAutocrafting().ifPresent(autocrafting -> {
            autocrafting.cancelAll();
            context.drainEnergy(
                Platform.INSTANCE.getConfig().getWirelessAutocraftingMonitor().getCancelAllEnergyUsage()
            );
        });
    }
}

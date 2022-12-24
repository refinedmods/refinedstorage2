package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExposedExternalStorage;
import com.refinedmods.refinedstorage2.api.network.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import javax.annotation.Nullable;

public class InterfaceNetworkNode<T> extends AbstractNetworkNode {
    private long energyUsage;
    private final StorageChannelType<T> storageChannelType;
    private final Actor actor = new NetworkNodeActor(this);
    @Nullable
    private InterfaceExportState<T> exportState;
    private long transferQuota;

    public InterfaceNetworkNode(final long energyUsage, final StorageChannelType<T> storageChannelType) {
        this.energyUsage = energyUsage;
        this.storageChannelType = storageChannelType;
    }

    public boolean isActingAsExternalStorage() {
        if (network == null) {
            return false;
        }
        return network
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType)
            .hasSource(this::isStorageAnExternalStorageProviderThatReferencesMe);
    }

    private boolean isStorageAnExternalStorageProviderThatReferencesMe(final Storage<T> storage) {
        return storage instanceof ExposedExternalStorage<T> proxy
            && proxy.getDelegate() != null
            && proxy.getDelegate().getProvider() instanceof InterfaceExternalStorageProvider<T> interfaceProvider
            && interfaceProvider.getInterface() == this;
    }

    @Nullable
    public InterfaceExportState<T> getExportState() {
        return exportState;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setExportState(@Nullable final InterfaceExportState<T> exportState) {
        this.exportState = exportState;
    }

    public void setTransferQuota(final long transferQuota) {
        this.transferQuota = CoreValidations.validateNotNegative(transferQuota, "Transfer quota cannot be negative");
    }

    @Override
    public void doWork() {
        super.doWork();
        if (exportState == null || network == null || !isActive() || transferQuota == 0) {
            return;
        }
        final StorageChannel<T> storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);
        for (int i = 0; i < exportState.getSlots(); ++i) {
            doExport(exportState, storageChannel, i);
        }
    }

    private void doExport(final InterfaceExportState<T> state,
                          final StorageChannel<T> storageChannel,
                          final int index) {
        final T want = state.getRequestedResource(index);
        final T got = state.getCurrentlyExportedResource(index);
        if (want == null && got != null) {
            clearExport(state, index, got, storageChannel);
        } else if (want != null && got == null) {
            doInitialExport(state, index, want, storageChannel);
        } else if (want != null) {
            final boolean valid = state.isCurrentlyExportedResourceValid(want, got);
            if (!valid) {
                clearExport(state, index, got, storageChannel);
            } else {
                doExportWithExistingResource(state, index, got, storageChannel);
            }
        }
    }

    private void clearExport(final InterfaceExportState<T> state,
                             final int slot,
                             final T got,
                             final StorageChannel<T> storageChannel) {
        final long currentAmount = state.getCurrentlyExportedResourceAmount(slot);
        final long inserted = storageChannel.insert(
            got,
            Math.min(currentAmount, transferQuota),
            Action.EXECUTE,
            actor
        );
        if (inserted == 0) {
            return;
        }
        state.decrementCurrentlyExportedAmount(slot, inserted);
    }

    private void doInitialExport(final InterfaceExportState<T> state,
                                 final int slot,
                                 final T want,
                                 final StorageChannel<T> storageChannel) {
        final long wantedAmount = state.getRequestedResourceAmount(slot);
        final Collection<T> candidates = state.expandExportCandidates(storageChannel, want);
        for (final T candidate : candidates) {
            final long extracted = storageChannel.extract(
                candidate,
                Math.min(transferQuota, wantedAmount),
                Action.EXECUTE,
                actor
            );
            if (extracted > 0) {
                state.setCurrentlyExported(slot, candidate, extracted);
                break;
            }
        }
    }

    private void doExportWithExistingResource(final InterfaceExportState<T> state,
                                              final int slot,
                                              final T got,
                                              final StorageChannel<T> storageChannel) {
        final long wantedAmount = state.getRequestedResourceAmount(slot);
        final long currentAmount = state.getCurrentlyExportedResourceAmount(slot);
        final long difference = wantedAmount - currentAmount;
        if (difference > 0) {
            exportAdditionalResources(state, slot, got, storageChannel, difference);
        } else if (difference < 0) {
            returnResource(state, slot, got, storageChannel, difference);
        }
    }

    private void exportAdditionalResources(final InterfaceExportState<T> state,
                                           final int slot,
                                           final T got,
                                           final StorageChannel<T> storageChannel,
                                           final long amount) {
        final long correctedAmount = Math.min(transferQuota, amount);
        final long extracted = storageChannel.extract(got, correctedAmount, Action.EXECUTE, actor);
        if (extracted == 0) {
            return;
        }
        state.incrementCurrentlyExportedAmount(slot, extracted);
    }

    private void returnResource(final InterfaceExportState<T> state,
                                final int slot,
                                final T got,
                                final StorageChannel<T> storageChannel,
                                final long amount) {
        final long correctedAmount = Math.min(transferQuota, Math.abs(amount));
        final long inserted = storageChannel.insert(got, correctedAmount, Action.EXECUTE, actor);
        if (inserted == 0) {
            return;
        }
        state.decrementCurrentlyExportedAmount(slot, inserted);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

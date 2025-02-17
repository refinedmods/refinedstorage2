package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExposedExternalStorage;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

public class InterfaceNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    @Nullable
    private InterfaceExportState exportState;
    private ToLongFunction<ResourceKey> transferQuotaProvider = resource -> Long.MAX_VALUE;

    public InterfaceNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferQuotaProvider(final ToLongFunction<ResourceKey> transferQuotaProvider) {
        this.transferQuotaProvider = transferQuotaProvider;
    }

    public boolean isActingAsExternalStorage() {
        if (network == null) {
            return false;
        }
        return network.getComponent(StorageNetworkComponent.class).hasSource(
            this::isStorageAnExternalStorageProviderThatReferencesMe
        );
    }

    private boolean isStorageAnExternalStorageProviderThatReferencesMe(final Storage storage) {
        return storage instanceof ExposedExternalStorage proxy
            && proxy.getExternalStorageProvider() instanceof InterfaceExternalStorageProvider interfaceProvider
            && interfaceProvider.getInterface() == this;
    }

    @Nullable
    public InterfaceExportState getExportState() {
        return exportState;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setExportState(@Nullable final InterfaceExportState exportState) {
        this.exportState = exportState;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (exportState == null || network == null || !isActive()) {
            return;
        }
        final StorageNetworkComponent storageComponent = network.getComponent(StorageNetworkComponent.class);
        for (int i = 0; i < exportState.getSlots(); ++i) {
            doExport(exportState, i, storageComponent);
        }
    }

    private void doExport(final InterfaceExportState state,
                          final int index,
                          final StorageNetworkComponent storageComponent) {
        final ResourceKey want = state.getRequestedResource(index);
        final ResourceKey got = state.getExportedResource(index);
        if (want == null && got != null) {
            clearExportedResource(state, index, got, storageComponent);
        } else if (want != null && got == null) {
            doInitialExport(state, index, want, storageComponent);
        } else if (want != null) {
            final boolean valid = state.isExportedResourceValid(want, got);
            if (!valid) {
                clearExportedResource(state, index, got, storageComponent);
            } else {
                doExportWithExistingResource(state, index, got, storageComponent);
            }
        }
    }

    private void clearExportedResource(final InterfaceExportState state,
                                       final int slot,
                                       final ResourceKey got,
                                       final RootStorage rootStorage) {
        final long currentAmount = state.getExportedAmount(slot);
        final long inserted = rootStorage.insert(
            got,
            Math.min(currentAmount, transferQuotaProvider.applyAsLong(got)),
            Action.EXECUTE,
            actor
        );
        if (inserted == 0) {
            return;
        }
        state.shrinkExportedAmount(slot, inserted);
    }

    private void doInitialExport(final InterfaceExportState state,
                                 final int slot,
                                 final ResourceKey want,
                                 final RootStorage rootStorage) {
        final long wantedAmount = state.getRequestedAmount(slot);
        final Collection<ResourceKey> candidates = state.expandExportCandidates(rootStorage, want);
        for (final ResourceKey candidate : candidates) {
            final long extracted = rootStorage.extract(
                candidate,
                Math.min(transferQuotaProvider.applyAsLong(want), wantedAmount),
                Action.EXECUTE,
                actor
            );
            if (extracted > 0) {
                state.setExportSlot(slot, candidate, extracted);
                break;
            }
        }
    }

    private void doExportWithExistingResource(final InterfaceExportState state,
                                              final int slot,
                                              final ResourceKey got,
                                              final StorageNetworkComponent storageComponent) {
        final long wantedAmount = state.getRequestedAmount(slot);
        final long currentAmount = state.getExportedAmount(slot);
        final long difference = wantedAmount - currentAmount;
        if (difference > 0) {
            exportAdditionalResources(state, slot, got, difference, storageComponent);
        } else if (difference < 0) {
            returnExportedResource(state, slot, got, difference, storageComponent);
        }
    }

    private void exportAdditionalResources(final InterfaceExportState state,
                                           final int slot,
                                           final ResourceKey got,
                                           final long amount,
                                           final RootStorage rootStorage) {
        final long correctedAmount = Math.min(transferQuotaProvider.applyAsLong(got), amount);
        final long extracted = rootStorage.extract(got, correctedAmount, Action.EXECUTE, actor);
        if (extracted == 0) {
            return;
        }
        state.growExportedAmount(slot, extracted);
    }

    private void returnExportedResource(final InterfaceExportState state,
                                        final int slot,
                                        final ResourceKey got,
                                        final long amount,
                                        final RootStorage rootStorage) {
        final long correctedAmount = Math.min(transferQuotaProvider.applyAsLong(got), Math.abs(amount));
        final long inserted = rootStorage.insert(got, correctedAmount, Action.EXECUTE, actor);
        if (inserted == 0) {
            return;
        }
        state.shrinkExportedAmount(slot, inserted);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}

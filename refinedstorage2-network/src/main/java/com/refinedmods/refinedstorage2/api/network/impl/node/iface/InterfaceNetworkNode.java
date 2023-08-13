package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExposedExternalStorage;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Collection;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

public class InterfaceNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    @Nullable
    private InterfaceExportState exportState;
    private ToLongFunction<ResourceTemplate<?>> transferQuotaProvider = resourceTemplate -> Long.MAX_VALUE;

    public InterfaceNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferQuotaProvider(final ToLongFunction<ResourceTemplate<?>> transferQuotaProvider) {
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

    private boolean isStorageAnExternalStorageProviderThatReferencesMe(final Storage<?> storage) {
        return storage instanceof ExposedExternalStorage<?> proxy
            && proxy.getExternalStorageProvider() instanceof InterfaceExternalStorageProvider<?> interfaceProvider
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
        final ResourceTemplate<?> want = state.getRequestedResource(index);
        final ResourceTemplate<?> got = state.getExportedResource(index);
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

    private <T> void clearExportedResource(final InterfaceExportState state,
                                           final int slot,
                                           final ResourceTemplate<T> got,
                                           final StorageNetworkComponent storageComponent) {
        final long currentAmount = state.getExportedAmount(slot);
        final StorageChannel<T> storageChannel = storageComponent.getStorageChannel(got.storageChannelType());
        final long inserted = storageChannel.insert(
            got.resource(),
            Math.min(currentAmount, transferQuotaProvider.applyAsLong(got)),
            Action.EXECUTE,
            actor
        );
        if (inserted == 0) {
            return;
        }
        state.shrinkExportedAmount(slot, inserted);
    }

    private <T> void doInitialExport(final InterfaceExportState state,
                                     final int slot,
                                     final ResourceTemplate<T> want,
                                     final StorageNetworkComponent storageComponent) {
        final long wantedAmount = state.getRequestedAmount(slot);
        final StorageChannel<T> storageChannel = storageComponent.getStorageChannel(want.storageChannelType());
        final Collection<T> candidates = state.expandExportCandidates(storageChannel, want.resource());
        for (final T candidate : candidates) {
            final long extracted = storageChannel.extract(
                candidate,
                Math.min(transferQuotaProvider.applyAsLong(want), wantedAmount),
                Action.EXECUTE,
                actor
            );
            if (extracted > 0) {
                state.setExportSlot(
                    slot,
                    new ResourceTemplate<>(candidate, want.storageChannelType()),
                    extracted
                );
                break;
            }
        }
    }

    private <T> void doExportWithExistingResource(final InterfaceExportState state,
                                                  final int slot,
                                                  final ResourceTemplate<T> got,
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

    private <T> void exportAdditionalResources(final InterfaceExportState state,
                                               final int slot,
                                               final ResourceTemplate<T> got,
                                               final long amount,
                                               final StorageNetworkComponent storageComponent) {
        final long correctedAmount = Math.min(transferQuotaProvider.applyAsLong(got), amount);
        final StorageChannel<T> storageChannel = storageComponent.getStorageChannel(got.storageChannelType());
        final long extracted = storageChannel.extract(got.resource(), correctedAmount, Action.EXECUTE, actor);
        if (extracted == 0) {
            return;
        }
        state.growExportedAmount(slot, extracted);
    }

    private <T> void returnExportedResource(final InterfaceExportState state,
                                            final int slot,
                                            final ResourceTemplate<T> got,
                                            final long amount,
                                            final StorageNetworkComponent storageComponent) {
        final StorageChannel<T> storageChannel = storageComponent.getStorageChannel(got.storageChannelType());
        final long correctedAmount = Math.min(transferQuotaProvider.applyAsLong(got), Math.abs(amount));
        final long inserted = storageChannel.insert(got.resource(), correctedAmount, Action.EXECUTE, actor);
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

package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridWatchers;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.support.networkbounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.common.Platform;

class WirelessGridOperations<T> implements GridOperations<T> {
    private final GridOperations<T> delegate;
    private final NetworkBoundItemSession session;
    private final GridWatchers watchers;

    WirelessGridOperations(final GridOperations<T> delegate,
                           final NetworkBoundItemSession session,
                           final GridWatchers watchers) {
        this.delegate = delegate;
        this.session = session;
        this.watchers = watchers;
    }

    @Override
    public boolean extract(final T resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage<T> destination) {
        final boolean success = delegate.extract(resource, extractMode, destination);
        if (success) {
            drain(Platform.INSTANCE.getConfig().getWirelessGrid().getExtractEnergyUsage());
        }
        return success;
    }

    @Override
    public boolean insert(final T resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage<T> source) {
        final boolean success = delegate.insert(resource, insertMode, source);
        if (success) {
            drain(Platform.INSTANCE.getConfig().getWirelessGrid().getInsertEnergyUsage());
        }
        return success;
    }

    private void drain(final long amount) {
        final boolean wasActive = session.isActive();
        session.drainEnergy(amount);
        final boolean isActive = session.isActive();
        if (wasActive != isActive) {
            watchers.activeChanged(isActive);
        }
    }
}

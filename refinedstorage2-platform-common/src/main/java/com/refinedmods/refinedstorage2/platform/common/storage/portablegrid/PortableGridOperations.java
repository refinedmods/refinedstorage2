package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class PortableGridOperations<T> implements GridOperations<T> {
    private final GridOperations<T> delegate;
    private final EnergyStorage energyStorage;

    public PortableGridOperations(final GridOperations<T> delegate, final EnergyStorage energyStorage) {
        this.delegate = delegate;
        this.energyStorage = energyStorage;
    }

    @Override
    public boolean extract(final T resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage<T> destination) {
        if (delegate.extract(resource, extractMode, destination)) {
            energyStorage.extract(
                Platform.INSTANCE.getConfig().getPortableGrid().getExtractEnergyUsage(),
                Action.EXECUTE
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean insert(final T resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage<T> source) {
        if (delegate.insert(resource, insertMode, source)) {
            energyStorage.extract(
                Platform.INSTANCE.getConfig().getPortableGrid().getInsertEnergyUsage(),
                Action.EXECUTE
            );
            return true;
        }
        return false;
    }
}

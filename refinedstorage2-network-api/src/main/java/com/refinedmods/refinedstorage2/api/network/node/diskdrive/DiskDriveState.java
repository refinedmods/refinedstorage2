package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import java.util.function.IntFunction;

public class DiskDriveState {
    private final StorageDiskState[] states;

    private DiskDriveState(final StorageDiskState[] states) {
        this.states = states;
    }

    public StorageDiskState getState(final int id) {
        return states[id];
    }

    public StorageDiskState[] getStates() {
        return states;
    }

    public static DiskDriveState of(final int count, final IntFunction<StorageDiskState> provider) {
        final StorageDiskState[] states = new StorageDiskState[count];
        for (int i = 0; i < count; ++i) {
            states[i] = provider.apply(i);
        }
        return new DiskDriveState(states);
    }
}

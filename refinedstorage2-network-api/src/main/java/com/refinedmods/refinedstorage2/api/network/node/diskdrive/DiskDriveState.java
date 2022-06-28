package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import java.util.Arrays;

import com.google.common.base.Preconditions;

public class DiskDriveState {
    private final StorageDiskState[] states;

    public DiskDriveState(final int disks) {
        this.states = new StorageDiskState[disks];
        Arrays.fill(states, StorageDiskState.NONE);
    }

    public void setState(final int id, final StorageDiskState state) {
        Preconditions.checkNotNull(state, "State cannot be null");
        this.states[id] = state;
    }

    public StorageDiskState getState(final int id) {
        return states[id];
    }

    public StorageDiskState[] getStates() {
        return states;
    }
}

package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import java.util.Arrays;

public class DiskDriveState {
    private final StorageDiskState[] states;

    public DiskDriveState(int disks) {
        this.states = new StorageDiskState[disks];
        Arrays.fill(states, StorageDiskState.NONE);
    }

    public void setState(int id, StorageDiskState state) {
        this.states[id] = state;
    }

    public StorageDiskState getState(int id) {
        return states[id];
    }

    public StorageDiskState[] getStates() {
        return states;
    }
}

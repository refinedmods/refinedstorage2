package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.disk.DiskState;

import java.util.Arrays;

public class DiskDriveState {
    private final DiskState[] states;

    public DiskDriveState(int disks) {
        this.states = new DiskState[disks];
        Arrays.fill(states, DiskState.NONE);
    }

    public void setState(int id, DiskState state) {
        this.states[id] = state;
    }

    public DiskState getState(int id) {
        return states[id];
    }

    public DiskState[] getStates() {
        return states;
    }
}

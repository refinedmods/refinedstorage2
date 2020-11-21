package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.ListTag;

import java.util.Arrays;

public class DiskDriveState {
    private final DiskState[] states;

    public DiskDriveState(int disks) {
        this.states = new DiskState[disks];
        Arrays.fill(states, DiskState.NONE);
    }

    public DiskDriveState(ListTag list) {
        this.states = new DiskState[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            int idx = ((ByteTag) list.get(i)).getInt();
            if (idx < 0 || idx >= DiskState.values().length) {
                idx = DiskState.NONE.ordinal();
            }

            states[i] = DiskState.values()[idx];
        }
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

    public ListTag getTag() {
        ListTag list = new ListTag();
        for (DiskState state : states) {
            list.add(ByteTag.of((byte) state.ordinal()));
        }
        return list;
    }
}

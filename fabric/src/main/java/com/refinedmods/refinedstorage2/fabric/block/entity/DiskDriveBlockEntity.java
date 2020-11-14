package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiskDriveBlockEntity extends NetworkNodeBlockEntity implements RenderAttachmentBlockEntity {
    public enum DiskState {
        NONE(0),
        DISCONNECTED(0x323232),
        NORMAL(0x00E9FF),
        NEAR_CAPACITY(0xFFB700),
        FULL(0xDA4B40);

        private final int color;

        DiskState(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public DiskDriveBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive());
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        List<DiskState> states = new ArrayList<>();
        states.add(DiskState.NORMAL);
        states.add(DiskState.DISCONNECTED);
        states.add(DiskState.FULL);
        states.add(DiskState.NEAR_CAPACITY);
        states.add(DiskState.NORMAL);
        states.add(DiskState.NONE);
        states.add(DiskState.FULL);
        states.add(DiskState.NEAR_CAPACITY);
        return states;
    }
}

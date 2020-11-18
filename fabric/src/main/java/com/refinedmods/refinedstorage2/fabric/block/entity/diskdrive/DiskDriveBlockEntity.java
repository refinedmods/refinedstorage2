package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiskDriveBlockEntity extends NetworkNodeBlockEntity implements RenderAttachmentBlockEntity {
    private final DiskDriveInventory diskInventory = new DiskDriveInventory();

    public DiskDriveBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive());

        diskInventory.setOwnerListener((view, slot, previous, current) -> markDirty());
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains("inv")) {
            diskInventory.fromTag(tag.getCompound("inv"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        tag.put("inv", diskInventory.toTag());
        return tag;
    }

    public FixedItemInv getDiskInventory() {
        return diskInventory;
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

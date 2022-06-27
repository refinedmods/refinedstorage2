package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FabricDiskDriveBlockEntity extends DiskDriveBlockEntity implements RenderAttachmentBlockEntity {
    public FabricDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }
}

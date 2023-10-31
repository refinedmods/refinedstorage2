package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricDiskDriveBlockEntity extends AbstractDiskDriveBlockEntity implements RenderAttachmentBlockEntity {
    public FabricDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderAttachmentData() {
        return driveState;
    }
}

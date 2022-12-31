package com.refinedmods.refinedstorage2.platform.forge.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ForgeDiskDriveBlockEntity extends AbstractDiskDriveBlockEntity {
    public static final ModelProperty<DiskDriveState> STATE_PROPERTY = new ModelProperty<>();

    public ForgeDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    protected void onDriveStateUpdated() {
        requestModelDataUpdate();
        super.onDriveStateUpdated();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(STATE_PROPERTY, driveState).build();
    }
}

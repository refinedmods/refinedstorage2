package com.refinedmods.refinedstorage2.platform.forge.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(STATE_PROPERTY, driveState).build();
    }
}

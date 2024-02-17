package com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ForgeDiskDriveBlockEntity extends AbstractDiskDriveBlockEntity {
    public static final ModelProperty<Disk[]> DISKS_PROPERTY = new ModelProperty<>();

    public ForgeDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    protected void onClientDriveStateUpdated() {
        requestModelDataUpdate();
        super.onClientDriveStateUpdated();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(DISKS_PROPERTY, disks).build();
    }
}

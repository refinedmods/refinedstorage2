package com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface;

import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ForgeDiskInterfaceBlockEntity extends AbstractDiskInterfaceBlockEntity {
    public static final ModelProperty<Disk[]> DISKS_PROPERTY = new ModelProperty<>();

    public ForgeDiskInterfaceBlockEntity(final BlockPos pos, final BlockState state) {
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

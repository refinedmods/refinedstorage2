package com.refinedmods.refinedstorage.neoforge.storage.diskinterface;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class ForgeDiskInterfaceBlockEntity extends AbstractDiskInterfaceBlockEntity {
    public static final ModelProperty<Disk[]> DISKS_PROPERTY = new ModelProperty<>();

    public ForgeDiskInterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    public ModelData getModelData() {
        if (disks == null) {
            return ModelData.EMPTY;
        }
        return ModelData.builder().with(DISKS_PROPERTY, disks).build();
    }
}

package com.refinedmods.refinedstorage2.platform.forge.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.portablegrid.PortableGridType;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ForgePortableGridBlockEntity extends AbstractPortableGridBlockEntity {
    public static final ModelProperty<Disk> DISK_PROPERTY = new ModelProperty<>();

    public ForgePortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onDriveStateUpdated() {
        requestModelDataUpdate();
        super.onDriveStateUpdated();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(DISK_PROPERTY, disk).build();
    }
}

package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class ForgePortableGridBlockEntity extends AbstractPortableGridBlockEntity {
    public static final ModelProperty<Disk> DISK_PROPERTY = new ModelProperty<>();

    public ForgePortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public ModelData getModelData() {
        if (disk == null) {
            return ModelData.EMPTY;
        }
        return ModelData.builder().with(DISK_PROPERTY, disk).build();
    }
}

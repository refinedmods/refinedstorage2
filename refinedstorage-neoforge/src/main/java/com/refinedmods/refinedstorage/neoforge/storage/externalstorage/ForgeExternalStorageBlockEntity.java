package com.refinedmods.refinedstorage.neoforge.storage.externalstorage;

import com.refinedmods.refinedstorage.common.storage.externalstorage.AbstractExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.neoforge.support.render.ModelProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

public class ForgeExternalStorageBlockEntity extends AbstractExternalStorageBlockEntity {
    public ForgeExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(ModelProperties.CABLE_CONNECTIONS, connections).build();
    }
}

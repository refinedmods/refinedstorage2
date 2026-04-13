package com.refinedmods.refinedstorage.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage.common.storage.externalstorage.AbstractExternalStorageBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FabricExternalStorageBlockEntity extends AbstractExternalStorageBlockEntity {
    public FabricExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}

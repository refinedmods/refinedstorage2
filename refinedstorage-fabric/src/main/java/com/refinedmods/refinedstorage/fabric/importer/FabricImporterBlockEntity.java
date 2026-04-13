package com.refinedmods.refinedstorage.fabric.importer;

import com.refinedmods.refinedstorage.common.importer.AbstractImporterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FabricImporterBlockEntity extends AbstractImporterBlockEntity {
    public FabricImporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}

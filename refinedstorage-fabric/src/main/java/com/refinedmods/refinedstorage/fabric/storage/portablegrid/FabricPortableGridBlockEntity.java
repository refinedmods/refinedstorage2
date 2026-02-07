package com.refinedmods.refinedstorage.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FabricPortableGridBlockEntity extends AbstractPortableGridBlockEntity {
    public FabricPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return disk;
    }
}

package com.refinedmods.refinedstorage.platform.fabric.storage.diskinterface;

import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricDiskInterfaceBlockEntity extends AbstractDiskInterfaceBlockEntity {
    public FabricDiskInterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return disks;
    }
}

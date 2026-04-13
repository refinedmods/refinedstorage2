package com.refinedmods.refinedstorage.common.support.render;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class DiskLedsRenderState extends BlockEntityRenderState {
    public Disk @Nullable [] disks;
    @Nullable
    public OrientedDirection direction;
}

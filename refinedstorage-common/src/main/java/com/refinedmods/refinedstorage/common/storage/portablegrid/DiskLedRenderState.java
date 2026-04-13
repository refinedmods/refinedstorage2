package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class DiskLedRenderState extends BlockEntityRenderState {
    @Nullable
    public Disk disk;
    @Nullable
    public OrientedDirection direction;
}

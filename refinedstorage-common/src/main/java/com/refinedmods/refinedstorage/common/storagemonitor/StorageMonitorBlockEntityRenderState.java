package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class StorageMonitorBlockEntityRenderState extends BlockEntityRenderState {
    public boolean active;
    @Nullable
    public OrientedDirection direction;
    @Nullable
    public ResourceKey configuredResource;
    public long amount;
    public long seed;
}

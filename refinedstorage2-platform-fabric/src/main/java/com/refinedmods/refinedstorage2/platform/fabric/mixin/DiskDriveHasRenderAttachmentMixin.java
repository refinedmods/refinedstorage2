package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DiskDriveBlockEntity.class)
public abstract class DiskDriveHasRenderAttachmentMixin implements RenderAttachmentBlockEntity {
    @Shadow
    private DiskDriveState driveState;

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }
}

package com.refinedmods.refinedstorage.neoforge.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.support.render.RenderTypes;

import org.jspecify.annotations.Nullable;

public class ForgeDiskDriveBlockEntityRenderer<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskDriveBlockEntityRenderer<T> {
    public ForgeDiskDriveBlockEntityRenderer() {
        super(RenderTypes.DISK_LEDS);
    }

    @Override
    protected Disk @Nullable [] extractDisks(final AbstractDiskDriveBlockEntity blockEntity) {
        return blockEntity.getModelData().get(ForgeDiskDriveBlockEntity.DISKS_PROPERTY);
    }
}

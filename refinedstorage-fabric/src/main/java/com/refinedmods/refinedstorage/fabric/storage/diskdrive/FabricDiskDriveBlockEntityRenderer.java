package com.refinedmods.refinedstorage.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage.fabric.support.render.RenderTypes;

import org.jspecify.annotations.Nullable;

public class FabricDiskDriveBlockEntityRenderer<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskDriveBlockEntityRenderer<T> {
    public FabricDiskDriveBlockEntityRenderer() {
        super(RenderTypes.DISK_LEDS);
    }

    @Override
    protected Disk @Nullable [] extractDisks(final AbstractDiskDriveBlockEntity blockEntity) {
        if (!(blockEntity instanceof FabricDiskDriveBlockEntity fabricBlockEntity)) {
            return null;
        }
        if (fabricBlockEntity.getRenderData() instanceof Disk[] disks) {
            return disks;
        }
        return null;
    }
}

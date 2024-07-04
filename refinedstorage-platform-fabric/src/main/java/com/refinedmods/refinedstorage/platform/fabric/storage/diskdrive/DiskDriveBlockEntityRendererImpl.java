package com.refinedmods.refinedstorage.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage.platform.fabric.support.render.RenderTypes;

public class DiskDriveBlockEntityRendererImpl<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskDriveBlockEntityRenderer<T> {
    public DiskDriveBlockEntityRendererImpl() {
        super(RenderTypes.DISK_LED);
    }

    @Override
    protected Disk[] getDisks(final AbstractDiskDriveBlockEntity blockEntity) {
        if (!(blockEntity instanceof FabricDiskDriveBlockEntity fabricBlockEntity)) {
            return null;
        }
        if (fabricBlockEntity.getRenderData() instanceof Disk[] disks) {
            return disks;
        }
        return null;
    }
}

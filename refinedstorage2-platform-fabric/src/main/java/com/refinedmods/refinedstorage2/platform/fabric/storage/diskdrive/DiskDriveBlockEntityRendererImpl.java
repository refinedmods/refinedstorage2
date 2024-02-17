package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.RenderTypes;

public class DiskDriveBlockEntityRendererImpl<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskDriveBlockEntityRenderer<T> {
    public DiskDriveBlockEntityRendererImpl() {
        super(RenderTypes.DISK_LED);
    }

    @Override
    protected Disk[] getDisks(final AbstractDiskDriveBlockEntity blockEntity) {
        if (!(blockEntity instanceof FabricDiskDriveBlockEntity fabricDiskDriveBlockEntity)) {
            return null;
        }
        if (fabricDiskDriveBlockEntity.getRenderAttachmentData() instanceof Disk[] disks) {
            return disks;
        }
        return null;
    }
}

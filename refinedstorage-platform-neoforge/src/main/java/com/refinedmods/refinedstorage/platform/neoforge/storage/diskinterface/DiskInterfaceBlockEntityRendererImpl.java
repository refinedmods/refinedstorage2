package com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface;

import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.AbstractDiskInterfaceBlockEntityRenderer;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.RenderTypes;

public class DiskInterfaceBlockEntityRendererImpl<T extends AbstractDiskInterfaceBlockEntity>
    extends AbstractDiskInterfaceBlockEntityRenderer<T> {
    public DiskInterfaceBlockEntityRendererImpl() {
        super(RenderTypes.DISK_LED);
    }

    @Override
    protected Disk[] getDisks(final AbstractDiskInterfaceBlockEntity blockEntity) {
        return blockEntity.getModelData().get(ForgeDiskInterfaceBlockEntity.DISKS_PROPERTY);
    }
}

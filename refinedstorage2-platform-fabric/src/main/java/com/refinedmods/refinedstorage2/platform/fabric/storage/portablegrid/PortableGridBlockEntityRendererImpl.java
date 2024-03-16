package com.refinedmods.refinedstorage2.platform.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.AbstractPortableGridBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.RenderTypes;

import javax.annotation.Nullable;

public class PortableGridBlockEntityRendererImpl<T extends AbstractPortableGridBlockEntity>
    extends AbstractPortableGridBlockEntityRenderer<T> {
    public PortableGridBlockEntityRendererImpl() {
        super(RenderTypes.DISK_LED);
    }

    @Override
    @Nullable
    protected Disk getDisk(final T blockEntity) {
        if (!(blockEntity instanceof FabricPortableGridBlockEntity fabricDiskDriveBlockEntity)) {
            return null;
        }
        if (fabricDiskDriveBlockEntity.getRenderData() instanceof Disk disk) {
            return disk;
        }
        return null;
    }
}

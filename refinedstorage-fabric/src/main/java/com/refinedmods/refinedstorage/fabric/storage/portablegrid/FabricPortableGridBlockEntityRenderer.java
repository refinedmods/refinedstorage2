package com.refinedmods.refinedstorage.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntityRenderer;
import com.refinedmods.refinedstorage.fabric.support.render.RenderTypes;

import org.jspecify.annotations.Nullable;

public class FabricPortableGridBlockEntityRenderer<T extends AbstractPortableGridBlockEntity>
    extends AbstractPortableGridBlockEntityRenderer<T> {
    public FabricPortableGridBlockEntityRenderer() {
        super(RenderTypes.DISK_LEDS);
    }

    @Override
    protected @Nullable Disk extractDisk(final T blockEntity) {
        if (!(blockEntity instanceof FabricPortableGridBlockEntity fabricDiskDriveBlockEntity)) {
            return null;
        }
        if (fabricDiskDriveBlockEntity.getRenderData() instanceof Disk disk) {
            return disk;
        }
        return null;
    }
}

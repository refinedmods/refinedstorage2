package com.refinedmods.refinedstorage.neoforge.storage.diskinterface;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.support.render.RenderTypes;

import org.jspecify.annotations.Nullable;

public class ForgeDiskInterfaceBlockEntityRenderer<T extends AbstractDiskInterfaceBlockEntity>
    extends AbstractDiskInterfaceBlockEntityRenderer<T> {
    public ForgeDiskInterfaceBlockEntityRenderer() {
        super(RenderTypes.DISK_LEDS);
    }

    @Override
    protected Disk @Nullable [] extractDisks(final T blockEntity) {
        return blockEntity.getModelData().get(ForgeDiskInterfaceBlockEntity.DISKS_PROPERTY);
    }
}

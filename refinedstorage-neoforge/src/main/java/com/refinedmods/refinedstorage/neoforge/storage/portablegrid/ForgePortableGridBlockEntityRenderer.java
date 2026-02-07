package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.support.render.RenderTypes;

import org.jspecify.annotations.Nullable;

public class ForgePortableGridBlockEntityRenderer<T extends AbstractPortableGridBlockEntity>
    extends AbstractPortableGridBlockEntityRenderer<T> {
    public ForgePortableGridBlockEntityRenderer() {
        super(RenderTypes.DISK_LEDS);
    }

    @Override
    protected @Nullable Disk extractDisk(final T blockEntity) {
        return blockEntity.getModelData().get(ForgePortableGridBlockEntity.DISK_PROPERTY);
    }
}

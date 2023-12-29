package com.refinedmods.refinedstorage2.platform.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.PortableGridType;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPortableGridBlockEntity extends AbstractPortableGridBlockEntity
    implements RenderAttachmentBlockEntity {
    public FabricPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nullable
    public Object getRenderAttachmentData() {
        return disk;
    }
}

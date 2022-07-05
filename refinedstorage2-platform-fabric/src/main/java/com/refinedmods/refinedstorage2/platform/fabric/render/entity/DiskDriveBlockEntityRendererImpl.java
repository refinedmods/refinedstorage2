package com.refinedmods.refinedstorage2.platform.fabric.render.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.render.entity.AbstractDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricDiskDriveBlockEntity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class DiskDriveBlockEntityRendererImpl<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskDriveBlockEntityRenderer<T> {
    private static final RenderType RENDER_TYPE = RenderType.create(
        "drive_leds",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        32565,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
            .createCompositeState(false)
    );

    public DiskDriveBlockEntityRendererImpl() {
        super(RENDER_TYPE);
    }

    @Override
    protected DiskDriveState getDriveState(final AbstractDiskDriveBlockEntity blockEntity) {
        if (!(blockEntity instanceof FabricDiskDriveBlockEntity fabricDiskDriveBlockEntity)) {
            return null;
        }
        if (fabricDiskDriveBlockEntity.getRenderAttachmentData() instanceof DiskDriveState driveState) {
            return driveState;
        }
        return null;
    }
}

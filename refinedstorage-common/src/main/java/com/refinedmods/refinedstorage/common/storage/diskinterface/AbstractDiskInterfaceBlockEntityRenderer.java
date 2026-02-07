package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.render.DiskLedsCustomGeometryRenderer;
import com.refinedmods.refinedstorage.common.support.render.DiskLedsRenderState;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDiskInterfaceBlockEntityRenderer<T extends AbstractDiskInterfaceBlockEntity>
    implements BlockEntityRenderer<T, DiskLedsRenderState> {
    private final RenderType renderType;

    protected AbstractDiskInterfaceBlockEntityRenderer(final RenderType renderType) {
        this.renderType = renderType;
    }

    protected abstract Disk @Nullable [] extractDisks(T blockEntity);

    @Override
    public DiskLedsRenderState createRenderState() {
        return new DiskLedsRenderState();
    }

    @Override
    public void extractRenderState(final T blockEntity, final DiskLedsRenderState state, final float partialTicks,
                                   final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.disks = extractDisks(blockEntity);
        state.direction = Blocks.INSTANCE.getDiskInterface().getDefault().getDirection(blockEntity.getBlockState());
    }

    @Override
    public void submit(final DiskLedsRenderState state, final PoseStack poseStack,
                       final SubmitNodeCollector submitNodeCollector, final CameraRenderState cameraRenderState) {
        if (state.disks == null || state.direction == null) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, new DiskLedsCustomGeometryRenderer(
            state.direction,
            state.disks,
            DiskLedsCustomGeometryRenderer.Type.DISK_INTERFACE
        ));
    }
}

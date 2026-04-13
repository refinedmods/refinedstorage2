package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.storage.Disk;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractPortableGridBlockEntityRenderer<T extends AbstractPortableGridBlockEntity>
    implements BlockEntityRenderer<T, DiskLedRenderState> {
    private final RenderType renderType;

    protected AbstractPortableGridBlockEntityRenderer(final RenderType renderType) {
        this.renderType = renderType;
    }

    protected abstract @Nullable Disk extractDisk(T blockEntity);

    @Override
    public DiskLedRenderState createRenderState() {
        return new DiskLedRenderState();
    }

    @Override
    public void extractRenderState(final T blockEntity, final DiskLedRenderState state, final float partialTicks,
                                   final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.disk = extractDisk(blockEntity);
        state.direction = Blocks.INSTANCE.getPortableGrid().getDirection(blockEntity.getBlockState());
    }

    @Override
    public void submit(final DiskLedRenderState state, final PoseStack poseStack,
                       final SubmitNodeCollector submitNodeCollector, final CameraRenderState cameraRenderState) {
        if (state.disk == null || state.direction == null) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, new DiskLedCustomGeometryRenderer(
            state.direction,
            state.disk
        ));
    }
}

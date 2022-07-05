package com.refinedmods.refinedstorage2.platform.common.render.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.render.CubeBuilder;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDiskDriveBlockEntityRenderer<T extends AbstractDiskDriveBlockEntity>
    implements BlockEntityRenderer<T> {
    private static final int LED_X1 = 10;
    private static final int LED_Y1 = 12;
    private static final int LED_Z1 = -1;

    private static final int LED_X2 = 11;
    private static final int LED_Y2 = 13;
    private static final int LED_Z2 = 0;

    private final RenderType renderType;

    protected AbstractDiskDriveBlockEntityRenderer(final RenderType renderType) {
        this.renderType = renderType;
    }

    @Nullable
    protected abstract DiskDriveState getDriveState(T blockEntity);

    @Override
    public void render(final T entity,
                       final float tickDelta,
                       final PoseStack poseStack,
                       final MultiBufferSource vertexConsumers,
                       final int light,
                       final int overlay) {
        final Level level = entity.getLevel();
        if (level == null) {
            return;
        }

        // Always sanity check the block state first, these may not always be correct and can cause crashes (see #20).
        final BlockState blockState = level.getBlockState(entity.getBlockPos());
        if (blockState.hasProperty(AbstractBaseBlock.DIRECTION)) {
            render(entity, poseStack, vertexConsumers, blockState.getValue(AbstractBaseBlock.DIRECTION));
        }
    }

    private void render(final T entity,
                        final PoseStack poseStack,
                        final MultiBufferSource vertexConsumers,
                        final BiDirection direction) {
        final DiskDriveState driveState = getDriveState(entity);

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(direction.getQuaternion());
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderType);

        if (driveState != null) {
            renderDisks(poseStack, driveState, vertexConsumer);
        }

        poseStack.popPose();
    }

    private void renderDisks(final PoseStack poseStack,
                             final DiskDriveState driveState,
                             final VertexConsumer vertexConsumer) {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                final StorageDiskState state = driveState.getState(i++);
                renderDisk(poseStack, vertexConsumer, y, x, state);
            }
        }
    }

    private void renderDisk(final PoseStack poseStack,
                            final VertexConsumer vertexConsumer,
                            final int y,
                            final int x,
                            final StorageDiskState state) {
        if (state == StorageDiskState.NONE) {
            return;
        }

        final float x1 = LED_X1 - (x * 7F);
        final float y1 = LED_Y1 - (y * 3F);

        final float x2 = LED_X2 - (x * 7F);
        final float y2 = LED_Y2 - (y * 3F);

        CubeBuilder.INSTANCE.putCube(
            poseStack,
            vertexConsumer,
            x1 / 16F,
            y1 / 16F,
            LED_Z1 / 16F,
            x2 / 16F,
            y2 / 16F,
            LED_Z2 / 16F,
            state.getColor() >> 16 & 0xFF,
            state.getColor() >> 8 & 0xFF,
            state.getColor() & 0xFF,
            255,
            Direction.SOUTH
        );
    }
}

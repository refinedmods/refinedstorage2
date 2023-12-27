package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.StorageState;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.render.CubeBuilder;

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
    protected abstract DiskDriveDisk[] getDisks(T blockEntity);

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
        if (blockState.getBlock() instanceof DiskDriveBlock diskDriveBlock) {
            final BiDirection direction = diskDriveBlock.getDirection(blockState);
            if (direction != null) {
                render(entity, poseStack, vertexConsumers, direction);
            }
        }
    }

    private void render(final T entity,
                        final PoseStack poseStack,
                        final MultiBufferSource vertexConsumers,
                        final BiDirection direction) {
        final DiskDriveDisk[] disks = getDisks(entity);

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(direction.getQuaternion());
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderType);

        if (disks != null) {
            renderDisks(poseStack, disks, vertexConsumer);
        }

        poseStack.popPose();
    }

    private void renderDisks(final PoseStack poseStack,
                             final DiskDriveDisk[] disks,
                             final VertexConsumer vertexConsumer) {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                final DiskDriveDisk disk = disks[i++];
                renderDisk(poseStack, vertexConsumer, y, x, disk);
            }
        }
    }

    private void renderDisk(final PoseStack poseStack,
                            final VertexConsumer vertexConsumer,
                            final int y,
                            final int x,
                            final DiskDriveDisk disk) {
        if (disk.state() == StorageState.NONE) {
            return;
        }
        final int color = getColor(disk.state());
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
            color >> 16 & 0xFF,
            color >> 8 & 0xFF,
            color & 0xFF,
            255,
            Direction.SOUTH
        );
    }

    private int getColor(final StorageState state) {
        return switch (state) {
            case NONE -> 0;
            case INACTIVE -> 0x323232;
            case NORMAL -> 0x00E9FF;
            case NEAR_CAPACITY -> 0xFFB700;
            case FULL -> 0xDA4B40;
        };
    }
}

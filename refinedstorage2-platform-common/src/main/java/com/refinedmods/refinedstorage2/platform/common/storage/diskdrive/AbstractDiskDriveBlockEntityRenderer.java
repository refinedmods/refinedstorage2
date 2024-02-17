package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.render.AbstractDiskLedBlockEntityRenderer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDiskDriveBlockEntityRenderer<T extends AbstractDiskDriveBlockEntity>
    extends AbstractDiskLedBlockEntityRenderer<T> {
    private final RenderType renderType;

    protected AbstractDiskDriveBlockEntityRenderer(final RenderType renderType) {
        this.renderType = renderType;
    }

    @Nullable
    protected abstract Disk[] getDisks(T blockEntity);

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
        final BlockState blockState = level.getBlockState(entity.getBlockPos());
        if (!(blockState.getBlock() instanceof DiskDriveBlock diskDriveBlock)) {
            return;
        }
        final BiDirection direction = diskDriveBlock.getDirection(blockState);
        if (direction == null) {
            return;
        }
        render(entity, poseStack, vertexConsumers, direction);
    }

    private void render(final T entity,
                        final PoseStack poseStack,
                        final MultiBufferSource vertexConsumers,
                        final BiDirection direction) {
        final Disk[] disks = getDisks(entity);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(direction.getQuaternion());
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderType);
        if (disks != null) {
            renderDiskLeds(poseStack, disks, vertexConsumer);
        }
        poseStack.popPose();
    }

    private void renderDiskLeds(final PoseStack poseStack,
                                final Disk[] disks,
                                final VertexConsumer vertexConsumer) {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                final Disk disk = disks[i++];
                renderLed(poseStack, vertexConsumer, 10 - (x * 7), 12 - (y * 3), -1, disk, Direction.SOUTH);
            }
        }
    }
}

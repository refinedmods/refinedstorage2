package com.refinedmods.refinedstorage2.platform.common.portablegrid;

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

public abstract class AbstractPortableGridBlockEntityRenderer<T extends AbstractPortableGridBlockEntity>
    extends AbstractDiskLedBlockEntityRenderer<T> {
    private final RenderType renderType;

    protected AbstractPortableGridBlockEntityRenderer(final RenderType renderType) {
        this.renderType = renderType;
    }

    @Nullable
    protected abstract Disk getDisk(T blockEntity);

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
        if (!(blockState.getBlock() instanceof PortableGridBlock portableGridBlock)) {
            return;
        }
        final BiDirection direction = portableGridBlock.getDirection(blockState);
        if (direction == null) {
            return;
        }
        render(entity, poseStack, vertexConsumers, direction);
    }

    private void render(final T entity,
                        final PoseStack poseStack,
                        final MultiBufferSource vertexConsumers,
                        final BiDirection direction) {
        final Disk disk = getDisk(entity);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(direction.getQuaternion());
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderType);
        if (disk != null) {
            renderLed(poseStack, vertexConsumer, -1, 2, 12, disk, Direction.EAST);
        }
        poseStack.popPose();
    }
}

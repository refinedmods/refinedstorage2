package com.refinedmods.refinedstorage2.platform.common.render.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.render.CubeBuilder;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DiskDriveBlockEntityRenderer<T extends DiskDriveBlockEntity> implements BlockEntityRenderer<T> {
    private static final int LED_X1 = 10;
    private static final int LED_Y1 = 12;
    private static final int LED_Z1 = -1;

    private static final int LED_X2 = 11;
    private static final int LED_Y2 = 13;
    private static final int LED_Z2 = 0;

    private final RenderType renderType;

    protected DiskDriveBlockEntityRenderer(RenderType renderType) {
        this.renderType = renderType;
    }

    protected abstract DiskDriveState getDriveState(T diskDriveBlockEntity);

    @Override
    public void render(T entity, float tickDelta, PoseStack poseStack, MultiBufferSource vertexConsumers, int light, int overlay) {
        DiskDriveState driveState = getDriveState(entity);

        // Always sanity check the block state first, these may not always be correct and can cause crashes (see #20).
        BlockState blockState = entity.getLevel().getBlockState(entity.getBlockPos());
        if (!blockState.hasProperty(BaseBlock.DIRECTION)) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(createQuaternion(blockState.getValue(BaseBlock.DIRECTION)));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderType);

        if (driveState != null) {
            renderDisks(poseStack, driveState, vertexConsumer);
        }

        poseStack.popPose();
    }

    private void renderDisks(PoseStack poseStack, DiskDriveState driveState, VertexConsumer vertexConsumer) {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                StorageDiskState state = driveState.getState(i++);
                renderDisk(poseStack, vertexConsumer, y, x, state);
            }
        }
    }

    private void renderDisk(PoseStack poseStack, VertexConsumer vertexConsumer, int y, int x, StorageDiskState state) {
        if (state == StorageDiskState.NONE) {
            return;
        }

        float x1 = LED_X1 - (x * 7F);
        float y1 = LED_Y1 - (y * 3F);

        float x2 = LED_X2 - (x * 7F);
        float y2 = LED_Y2 - (y * 3F);

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

    private Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().x(), direction.getVec().y(), direction.getVec().z(), true);
    }
}

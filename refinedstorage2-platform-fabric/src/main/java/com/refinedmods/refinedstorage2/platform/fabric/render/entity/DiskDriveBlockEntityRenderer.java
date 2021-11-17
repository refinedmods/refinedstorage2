package com.refinedmods.refinedstorage2.platform.fabric.render.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.fabric.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.render.CubeBuilder;
import com.refinedmods.refinedstorage2.platform.fabric.util.BiDirection;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBlockEntityRenderer implements BlockEntityRenderer<DiskDriveBlockEntity> {
    private static final RenderType RENDER_LAYER = RenderType.create(
            "drive_leds",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            32565,
            false,
            true,
            RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader)).createCompositeState(false)
    );

    private static final int LED_X1 = 10;
    private static final int LED_Y1 = 12;
    private static final int LED_Z1 = -1;

    private static final int LED_X2 = 11;
    private static final int LED_Y2 = 13;
    private static final int LED_Z2 = 0;

    private Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().x(), direction.getVec().y(), direction.getVec().z(), true);
    }

    @Override
    public void render(DiskDriveBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (!(entity.getRenderAttachmentData() instanceof DiskDriveState)) {
            return;
        }

        // Always sanity check the block state first, these may not always be correct and can cause crashes (see #20).
        BlockState blockState = entity.getLevel().getBlockState(entity.getBlockPos());
        if (!blockState.hasProperty(BaseBlock.DIRECTION)) {
            return;
        }

        DiskDriveState diskStates = (DiskDriveState) entity.getRenderAttachmentData();

        matrices.pushPose();

        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.mulPose(createQuaternion(blockState.getValue(BaseBlock.DIRECTION)));
        matrices.translate(-0.5F, -0.5F, -0.5F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RENDER_LAYER);

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                StorageDiskState state = diskStates.getState(i++);

                if (state != StorageDiskState.NONE) {
                    float x1 = LED_X1 - (x * 7F);
                    float y1 = LED_Y1 - (y * 3F);

                    float x2 = LED_X2 - (x * 7F);
                    float y2 = LED_Y2 - (y * 3F);

                    CubeBuilder.INSTANCE.putCube(
                            matrices,
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
        }

        matrices.popPose();
    }
}

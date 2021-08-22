package com.refinedmods.refinedstorage2.fabric.render.entity;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.fabric.block.BaseBlock;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.render.CubeBuilder;
import com.refinedmods.refinedstorage2.fabric.util.BiDirection;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class DiskDriveBlockEntityRenderer implements BlockEntityRenderer<DiskDriveBlockEntity> {
    private static final RenderLayer RENDER_LAYER = RenderLayer.of(
            "drive_leds",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            32565,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder().shader(new RenderPhase.Shader(GameRenderer::getPositionColorShader)).build(false)
    );

    private static final int LED_X1 = 10;
    private static final int LED_Y1 = 12;
    private static final int LED_Z1 = -1;

    private static final int LED_X2 = 11;
    private static final int LED_Y2 = 13;
    private static final int LED_Z2 = 0;

    private Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().getX(), direction.getVec().getY(), direction.getVec().getZ(), true);
    }

    @Override
    public void render(DiskDriveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!(entity.getRenderAttachmentData() instanceof DiskDriveState)) {
            return;
        }

        DiskDriveState states = (DiskDriveState) entity.getRenderAttachmentData();

        matrices.push();

        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(createQuaternion(entity.getWorld().getBlockState(entity.getPos()).get(BaseBlock.DIRECTION)));
        matrices.translate(-0.5F, -0.5F, -0.5F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RENDER_LAYER);

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                DiskState state = states.getState(i++);

                if (state != DiskState.NONE) {
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

        matrices.pop();
    }
}

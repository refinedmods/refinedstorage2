package com.refinedmods.refinedstorage2.fabric.render.entity;

import com.refinedmods.refinedstorage2.fabric.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.fabric.block.entity.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.render.CubeBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import java.util.List;

public class DiskDriveBlockEntityRenderer extends BlockEntityRenderer<DiskDriveBlockEntity> {
    public static final RenderLayer RENDER_LAYER = RenderLayer.of("drive_leds", VertexFormats.POSITION_COLOR, 7, 32565, false, true, RenderLayer.MultiPhaseParameters.builder().build(false));

    private static final int LED_X1 = 10;
    private static final int LED_Y1 = 12;
    private static final int LED_Z1 = -1;
    private static final int LED_X2 = 11;
    private static final int LED_Y2 = 13;
    private static final int LED_Z2 = 0;

    public DiskDriveBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DiskDriveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        List<DiskDriveBlockEntity.DiskState> states = (List<DiskDriveBlockEntity.DiskState>) entity.getRenderAttachmentData();

        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(entity.getWorld().getBlockState(entity.getPos()).get(DiskDriveBlock.DIRECTION).getQuaternion());
        matrices.translate(-0.5F, -0.5F, -0.5F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RENDER_LAYER);

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                DiskDriveBlockEntity.DiskState state = states.get(i++);

                if (state != DiskDriveBlockEntity.DiskState.NONE) {
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

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

public class DiskDriveBlockEntityRenderer extends BlockEntityRenderer<DiskDriveBlockEntity> {
    public static final RenderLayer RENDER_LAYER = RenderLayer.of("drive_leds", VertexFormats.POSITION_COLOR, 7, 32565, false, true, RenderLayer.MultiPhaseParameters.builder().build(false));

    public DiskDriveBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DiskDriveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(entity.getWorld().getBlockState(entity.getPos()).get(DiskDriveBlock.DIRECTION).getQuaternion());
        matrices.translate(-0.5F, -0.5F, -0.5F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RENDER_LAYER);

        float x1 = 10;
        float y1 = 12;
        float z1 = -1;

        float x2 = 11;
        float y2 = 13;
        float z2 = 0;

        x1 /= 16F;
        y1 /= 16F;
        z1 /= 16F;

        x2 /= 16F;
        y2 /= 16F;
        z2 /= 16F;

        CubeBuilder.INSTANCE.putCube(
            matrices,
            vertexConsumer,
            x1,
            y1,
            z1,
            x2,
            y2,
            z2,
            0,
            255,
            0,
            255,
            Direction.SOUTH
        );

        matrices.pop();
    }
}

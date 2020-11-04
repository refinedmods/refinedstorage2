package com.refinedmods.refinedstorage2.fabric.render;

import com.refinedmods.refinedstorage2.fabric.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.fabric.block.entity.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.util.BiDirection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DiskDriveBlockEntityRenderer extends BlockEntityRenderer<DiskDriveBlockEntity> {
    public DiskDriveBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);

    }

    @Override
    public void render(DiskDriveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);

        BiDirection biDirection = entity.getWorld().getBlockState(entity.getPos()).get(DiskDriveBlock.DIRECTION);
        matrices.multiply(biDirection.getQuaternion());

        //BlockEntityRendererHelper.rotateToFace(matrices, dir);
        matrices.translate(-0.5, -0.5, -0.5);

        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(
            new Identifier("refinedstorage2:block/disk")
        );
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getText(sprite.getAtlas().getId()));

        int color = 0xFFFFFFFF;
        int r = (color >> 16 & 0xFF);
        int g = (color >> 8 & 0xFF);
        int b = (color & 0xFF);
        int a = (color >> 24 & 0xFF);

        CubeBuilder.UvVector northUv = new CubeBuilder.UvVector(
            sprite.getFrameU(3),
            sprite.getFrameU(8),
            sprite.getFrameV(0),
            sprite.getFrameV(2)
        );

        CubeBuilder.UvVector upUv = new CubeBuilder.UvVector(
            sprite.getFrameU(3),
            sprite.getFrameU(8),
            sprite.getFrameV(2),
            sprite.getFrameV(3)
        );

        CubeBuilder.INSTANCE.putFace(
            matrices,
            consumer,
            (11 / 16F),
            (14 / 16F),
            (-1 / 16F),
            (16 / 16F),
            (16 / 16F),
            0,
            r,
            g,
            b,
            a,
            15728880,
            Direction.NORTH,
            northUv
        );

        CubeBuilder.INSTANCE.putFace(
            matrices,
            consumer,
            (11 / 16F),
            (14 / 16F),
            (-1 / 16F),
            (16 / 16F),
            (16 / 16F),
            0,
            r,
            g,
            b,
            a,
            15728880,
            Direction.UP,
            upUv
        );

        matrices.pop();
    }
}

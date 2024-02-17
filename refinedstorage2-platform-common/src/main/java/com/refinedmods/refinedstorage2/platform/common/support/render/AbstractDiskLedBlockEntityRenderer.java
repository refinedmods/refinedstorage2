package com.refinedmods.refinedstorage2.platform.common.support.render;

import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractDiskLedBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    protected void renderLed(final PoseStack poseStack,
                             final VertexConsumer vertexConsumer,
                             final int x,
                             final int y,
                             final int z,
                             final Disk disk,
                             final Direction excludeDirection) {
        if (disk.state() == StorageState.NONE) {
            return;
        }
        final int color = getColor(disk.state());
        CubeBuilder.putCube(
            poseStack,
            vertexConsumer,
            x / 16F,
            y / 16F,
            z / 16F,
            (x + 1) / 16F,
            (y + 1) / 16F,
            (z + 1) / 16F,
            color >> 16 & 0xFF,
            color >> 8 & 0xFF,
            color & 0xFF,
            255,
            excludeDirection
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

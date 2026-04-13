package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.render.CubeBuilder;
import com.refinedmods.refinedstorage.common.support.render.DiskLedsCustomGeometryRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;

class DiskLedCustomGeometryRenderer implements SubmitNodeCollector.CustomGeometryRenderer {
    private final OrientedDirection direction;
    private final Disk disk;

    DiskLedCustomGeometryRenderer(final OrientedDirection direction, final Disk disk) {
        this.direction = direction;
        this.disk = disk;
    }

    @Override
    public void render(final PoseStack.Pose pose, final VertexConsumer vertexConsumer) {
        rotate(pose, direction);
        renderLed(pose, vertexConsumer, -1, 2, 12, Direction.EAST);
    }

    private void renderLed(final PoseStack.Pose pose,
                           final VertexConsumer vertexConsumer,
                           final int x,
                           final int y,
                           final int z,
                           final Direction excludeDirection) {
        if (disk.state() == StorageState.NONE) {
            return;
        }
        final int color = DiskLedsCustomGeometryRenderer.getColor(disk.state());
        CubeBuilder.putCube(
            pose,
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

    private static void rotate(final PoseStack.Pose pose, final OrientedDirection direction) {
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.rotate(direction.getQuaternion());
        pose.translate(-0.5F, -0.5F, -0.5F);
    }
}

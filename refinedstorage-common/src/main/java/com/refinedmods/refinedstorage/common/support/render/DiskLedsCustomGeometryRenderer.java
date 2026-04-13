package com.refinedmods.refinedstorage.common.support.render;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;

public class DiskLedsCustomGeometryRenderer implements SubmitNodeCollector.CustomGeometryRenderer {
    private final OrientedDirection direction;
    private final Disk[] disks;
    private final Type type;

    public DiskLedsCustomGeometryRenderer(final OrientedDirection direction, final Disk[] disks, final Type type) {
        this.direction = direction;
        this.disks = disks;
        this.type = type;
    }

    @Override
    public void render(final PoseStack.Pose pose, final VertexConsumer vertexConsumer) {
        rotate(pose, direction);
        if (type == Type.DISK_DRIVE) {
            int i = 0;
            for (int y = 0; y < type.rows; ++y) {
                for (int x = 0; x < 2; ++x) {
                    final Disk disk = disks[i++];
                    renderLed(pose, vertexConsumer, type.getX(x), type.getY(y), -1, disk, Direction.SOUTH);
                }
            }
        } else if (type == Type.DISK_INTERFACE) {
            for (int idx = 0; idx < disks.length; ++idx) {
                final Disk disk = disks[idx];
                final int x = idx < 3 ? 0 : 1;
                final int y = idx % 3;
                renderLed(pose, vertexConsumer, type.getX(x), type.getY(y), -1, disk, Direction.SOUTH);
            }
        }
    }

    protected void renderLed(final PoseStack.Pose pose,
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

    public static int getColor(final StorageState state) {
        return switch (state) {
            case NONE -> 0;
            case INACTIVE -> 0x323232;
            case NORMAL -> 0x00E9FF;
            case NEAR_CAPACITY -> 0xFFB700;
            case FULL -> 0xDA4B40;
        };
    }

    protected static void rotate(final PoseStack.Pose pose, final OrientedDirection direction) {
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.rotate(direction.getQuaternion());
        pose.translate(-0.5F, -0.5F, -0.5F);
    }

    public enum Type {
        DISK_DRIVE(4),
        DISK_INTERFACE(3);

        private final int rows;

        Type(final int rows) {
            this.rows = rows;
        }

        public int getX(final int x) {
            return 10 - (x * 7);
        }

        public int getY(final int y) {
            return switch (this) {
                case DISK_DRIVE -> 12 - (y * 3);
                case DISK_INTERFACE -> 8 - (y * 3);
            };
        }
    }
}

package com.refinedmods.refinedstorage2.platform.common.support.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;

public class CubeBuilder {
    public static final CubeBuilder INSTANCE = new CubeBuilder();

    private CubeBuilder() {
    }

    public void putCube(final PoseStack poseStack,
                        final VertexConsumer builder,
                        final float x1,
                        final float y1,
                        final float z1,
                        final float x2,
                        final float y2,
                        final float z2,
                        final int r,
                        final int g,
                        final int b,
                        final int a,
                        final Direction exclude) {
        poseStack.pushPose();
        for (final Direction face : Direction.values()) {
            if (face == exclude) {
                continue;
            }
            putFace(poseStack, builder, x1, y1, z1, x2, y2, z2, r, g, b, a, face);
        }
        poseStack.popPose();
    }

    public void putFace(final PoseStack poseStack,
                        final VertexConsumer builder,
                        final float x1,
                        final float y1,
                        final float z1,
                        final float x2,
                        final float y2,
                        final float z2,
                        final int r,
                        final int g,
                        final int b,
                        final int a,
                        final Direction face) {
        switch (face) {
            case DOWN -> {
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z1);
            }
            case UP -> {
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z1);
            }
            case NORTH -> {
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z1);
            }
            case SOUTH -> {
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z2);
            }
            case WEST -> {
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x1, y2, z1);
            }
            case EAST -> {
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z1);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y2, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, poseStack, r, g, b, a, x2, y1, z1);
            }
        }
    }

    private void putVertex(final VertexConsumer builder,
                           final PoseStack poseStack,
                           final int r,
                           final int g,
                           final int b,
                           final int a,
                           final float x,
                           final float y,
                           final float z) {
        builder.vertex(poseStack.last().pose(), x, y, z).color(r, g, b, a).endVertex();
    }
}

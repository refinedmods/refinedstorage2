package com.refinedmods.refinedstorage2.platform.fabric.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class CubeBuilder {
    public static final CubeBuilder INSTANCE = new CubeBuilder();

    private CubeBuilder() {
    }

    public void putCube(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
        putCube(matrixStack, builder, x1, y1, z1, x2, y2, z2, r, g, b, a, null);
    }

    public void putCube(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, Direction exclude) {
        matrixStack.push();
        for (Direction face : Direction.values()) {
            if (face != exclude) {
                putFace(matrixStack, builder, x1, y1, z1, x2, y2, z2, r, g, b, a, face);
            }
        }
        matrixStack.pop();
    }

    public void putFace(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, Direction face) {
        switch (face) {
            case DOWN -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z1);
            }
            case UP -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z1);
            }
            case NORTH -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z1);
            }
            case SOUTH -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z2);
            }
            case WEST -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x1, y2, z1);
            }
            case EAST -> {
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z1);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y2, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z2);
                this.putVertex(builder, matrixStack, r, g, b, a, x2, y1, z1);
            }
        }
    }

    private void putVertex(VertexConsumer builder, MatrixStack matrixStack, int r, int g, int b, int a, float x, float y, float z) {
        builder.vertex(matrixStack.peek().getModel(), x, y, z).color(r, g, b, a).next();
    }
}

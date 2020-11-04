package com.refinedmods.refinedstorage2.fabric.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class CubeBuilder {
    public static final CubeBuilder INSTANCE = new CubeBuilder();

    private final byte[] uvRotations = new byte[Direction.values().length];

    private CubeBuilder() {
    }

    public void putCube(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, int light, Sprite sprite) {
        putCube(matrixStack, builder, x1, y1, z1, x2, y2, z2, r, g, b, a, light, sprite, null);
    }

    public void putCube(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, int light, Sprite sprite, Direction exclude) {
        matrixStack.push();

        for (Direction face : Direction.values()) {
            if (face != exclude) {
                putFace(matrixStack, builder, x1, y1, z1, x2, y2, z2, r, g, b, a, light, face, getDefaultUv(face, sprite, x1, y1, z1, x2, y2, z2));
            }
        }

        matrixStack.pop();
    }

    public void putFace(MatrixStack matrixStack, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, int light, Direction face, UvVector uv) {
        switch (face) {
            case DOWN:
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x2, y1, z1, uv);
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x2, y1, z2, uv);
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x1, y1, z2, uv);
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x1, y1, z1, uv);
                break;
            case UP:
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x1, y2, z1, uv);
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x1, y2, z2, uv);
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x2, y2, z2, uv);
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x2, y2, z1, uv);
                break;
            case NORTH:
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x2, y2, z1, uv);
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x2, y1, z1, uv);
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x1, y1, z1, uv);
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x1, y2, z1, uv);
                break;
            case SOUTH:
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x1, y2, z2, uv);
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x1, y1, z2, uv);
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x2, y1, z2, uv);
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x2, y2, z2, uv);
                break;
            case WEST:
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x1, y1, z1, uv);
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x1, y1, z2, uv);
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x1, y2, z2, uv);
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x1, y2, z1, uv);
                break;
            case EAST:
                this.putVertexBR(builder, matrixStack, face, r, g, b, a, light, x2, y2, z1, uv);
                this.putVertexBL(builder, matrixStack, face, r, g, b, a, light, x2, y2, z2, uv);
                this.putVertexTL(builder, matrixStack, face, r, g, b, a, light, x2, y1, z2, uv);
                this.putVertexTR(builder, matrixStack, face, r, g, b, a, light, x2, y1, z1, uv);
                break;
        }

    }

    private UvVector getDefaultUv(Direction face, Sprite texture, float x1, float y1, float z1, float x2, float y2, float z2) {
        UvVector uv = new UvVector();

        switch (face) {
            case DOWN:
                uv.u1 = texture.getFrameU(x1 * 16);
                uv.v1 = texture.getFrameV(z1 * 16);
                uv.u2 = texture.getFrameU(x2 * 16);
                uv.v2 = texture.getFrameV(z2 * 16);
                break;
            case UP:
                uv.u1 = texture.getFrameU(x1 * 16);
                uv.v1 = texture.getFrameV(z1 * 16);
                uv.u2 = texture.getFrameU(x2 * 16);
                uv.v2 = texture.getFrameV(z2 * 16);
                break;
            case NORTH:
                uv.u1 = texture.getFrameU(x1 * 16);
                uv.v1 = texture.getFrameV(16 - y1 * 16);
                uv.u2 = texture.getFrameU(x2 * 16);
                uv.v2 = texture.getFrameV(16 - y2 * 16);
                break;
            case SOUTH:
                uv.u1 = texture.getFrameU(x1 * 16);
                uv.v1 = texture.getFrameV(16 - y1 * 16);
                uv.u2 = texture.getFrameU(x2 * 16);
                uv.v2 = texture.getFrameV(16 - y2 * 16);
                break;
            case WEST:
                uv.u1 = texture.getFrameU(z1 * 16);
                uv.v1 = texture.getFrameV(16 - y1 * 16);
                uv.u2 = texture.getFrameU(z2 * 16);
                uv.v2 = texture.getFrameV(16 - y2 * 16);
                break;
            case EAST:
                uv.u1 = texture.getFrameU(z2 * 16);
                uv.v1 = texture.getFrameV(16 - y1 * 16);
                uv.u2 = texture.getFrameU(z1 * 16);
                uv.v2 = texture.getFrameV(16 - y2 * 16);
                break;
        }

        return uv;
    }

    // uv.u1, uv.v1
    private void putVertexTL(VertexConsumer builder, MatrixStack matrixStack, Direction face, int r, int g, int b, int a, int light, float x, float y, float z, UvVector uv) {
        float u, v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u1;
                v = uv.v1;
                break;
            case 1: // 90° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
            case 2: // 180° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
            case 3: // 270° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
        }

        this.putVertex(builder, matrixStack, r, g, b, a, light, x, y, z, u, v);
    }

    // uv.u2, uv.v1
    private void putVertexTR(VertexConsumer builder, MatrixStack matrixStack, Direction face, int r, int g, int b, int a, int light, float x, float y, float z, UvVector uv) {
        float u, v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u2;
                v = uv.v1;
                break;
            case 1: // 90° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
            case 2: // 180° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
            case 3: // 270° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
        }

        this.putVertex(builder, matrixStack, r, g, b, a, light, x, y, z, u, v);
    }

    // uv.u2, uv.v2
    private void putVertexBR(VertexConsumer builder, MatrixStack matrixStack, Direction face, int r, int g, int b, int a, int light, float x, float y, float z, UvVector uv) {
        float u;
        float v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u2;
                v = uv.v2;
                break;
            case 1: // 90° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
            case 2: // 180° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
            case 3: // 270° clockwise
                u = uv.u1;
                v = uv.v2;
                break;
        }

        this.putVertex(builder, matrixStack, r, g, b, a, light, x, y, z, u, v);
    }

    // uv.u1, uv.v2
    private void putVertexBL(VertexConsumer builder, MatrixStack matrixStack, Direction face, int r, int g, int b, int a, int light, float x, float y, float z, UvVector uv) {

        float u;
        float v;

        switch (this.uvRotations[face.ordinal()]) {
            default:
            case 0:
                u = uv.u1;
                v = uv.v2;
                break;
            case 1: // 90° clockwise
                u = uv.u2;
                v = uv.v2;
                break;
            case 2: // 180° clockwise
                u = uv.u2;
                v = uv.v1;
                break;
            case 3: // 270° clockwise
                u = uv.u1;
                v = uv.v1;
                break;
        }

        this.putVertex(builder, matrixStack, r, g, b, a, light, x, y, z, u, v);
    }

    private void putVertex(VertexConsumer builder, MatrixStack matrixStack, int r, int g, int b, int a, int light, float x, float y, float z, float u, float v) {
        builder.vertex(matrixStack.peek().getModel(), x, y, z)
            .color(r, g, b, a)
            .texture(u, v)
            .light(light)
            .next();
    }

    public static final class UvVector {
        private float u1;
        private float u2;
        private float v1;
        private float v2;

        public UvVector(float u1, float u2, float v1, float v2) {
            this.u1 = u1;
            this.u2 = u2;
            this.v1 = v1;
            this.v2 = v2;
        }

        UvVector() {
        }
    }
}

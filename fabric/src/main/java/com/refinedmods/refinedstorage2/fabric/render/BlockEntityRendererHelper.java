package com.refinedmods.refinedstorage2.fabric.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;

public class BlockEntityRendererHelper {
    private BlockEntityRendererHelper() {
    }

    public static void rotateToFace(MatrixStack matrices, Direction face) {
        switch (face) {
            case UP:
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(270));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
                break;
            case DOWN:
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F));
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
                break;
            case EAST:
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
                break;
            case WEST:
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
                break;
            case NORTH:
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
                break;
            case SOUTH:
                break;
            default:
                break;
        }
    }
}

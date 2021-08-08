package com.refinedmods.refinedstorage2.fabric.render.model.baked;

import com.refinedmods.refinedstorage2.fabric.util.BiDirection;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vector4f;

public class QuadRotator implements RenderContext.QuadTransform {
    private final BiDirection direction;

    public QuadRotator(BiDirection biDirection) {
        this.direction = biDirection;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Vec3f tmp = new Vec3f();

        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, tmp);

            tmp.add(-0.5F, -0.5F, -0.5F);
            tmp.rotate(createQuaternion(direction));
            tmp.add(0.5F, 0.5F, 0.5F);

            quad.pos(i, tmp);

            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.rotate(createQuaternion(direction));
                quad.normal(i, tmp);
            }
        }

        Matrix4f mat = new Matrix4f();
        mat.loadIdentity();
        mat.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(direction.getVec().getX()));
        mat.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(direction.getVec().getY()));
        mat.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(direction.getVec().getZ()));

        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(rotate(cullFace, mat));
        }

        quad.nominalFace(rotate(nominalFace, mat));

        return true;
    }

    private Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().getX(), direction.getVec().getY(), direction.getVec().getZ(), true);
    }

    private Direction rotate(Direction facing, Matrix4f mat) {
        Vec3i dir = facing.getVector();
        Vector4f vec = new Vector4f((float) dir.getX(), (float) dir.getY(), (float) dir.getZ(), 1.0F);
        vec.transform(mat);
        return Direction.getFacing(vec.getX(), vec.getY(), vec.getZ());
    }
}

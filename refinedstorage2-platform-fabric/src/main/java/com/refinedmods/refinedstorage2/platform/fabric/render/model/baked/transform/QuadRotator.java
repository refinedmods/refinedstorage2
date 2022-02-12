package com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.transform;

import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class QuadRotator implements RenderContext.QuadTransform {
    private final BiDirection direction;

    public QuadRotator(BiDirection biDirection) {
        this.direction = biDirection;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Vector3f tmp = new Vector3f();

        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, tmp);

            tmp.add(-0.5F, -0.5F, -0.5F);
            tmp.transform(createQuaternion(direction));
            tmp.add(0.5F, 0.5F, 0.5F);

            quad.pos(i, tmp);

            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.transform(createQuaternion(direction));
                quad.normal(i, tmp);
            }
        }

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.multiply(Vector3f.XP.rotationDegrees(direction.getVec().x()));
        mat.multiply(Vector3f.YP.rotationDegrees(direction.getVec().y()));
        mat.multiply(Vector3f.ZP.rotationDegrees(direction.getVec().z()));

        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(rotate(cullFace, mat));
        }

        quad.nominalFace(rotate(nominalFace, mat));

        return true;
    }

    private Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().x(), direction.getVec().y(), direction.getVec().z(), true);
    }

    private Direction rotate(Direction facing, Matrix4f mat) {
        Vec3i dir = facing.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 1.0F);
        vec.transform(mat);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }
}

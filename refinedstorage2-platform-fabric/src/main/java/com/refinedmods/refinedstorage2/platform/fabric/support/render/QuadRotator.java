package com.refinedmods.refinedstorage2.platform.fabric.support.render;

import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class QuadRotator implements RenderContext.QuadTransform {
    private final BiDirection direction;

    public QuadRotator(final BiDirection biDirection) {
        this.direction = biDirection;
    }

    @Override
    public boolean transform(final MutableQuadView quad) {
        final Vector3f tmp = new Vector3f();

        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, tmp);

            tmp.add(-0.5F, -0.5F, -0.5F);
            tmp.rotate(direction.getQuaternion());
            tmp.add(0.5F, 0.5F, 0.5F);

            quad.pos(i, tmp);

            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.rotate(direction.getQuaternion());
                quad.normal(i, tmp);
            }
        }

        final Matrix4f mat = new Matrix4f().rotationXYZ(
            direction.getVec().x() * Mth.DEG_TO_RAD,
            direction.getVec().y() * Mth.DEG_TO_RAD,
            direction.getVec().z() * Mth.DEG_TO_RAD
        );

        final Direction nominalFace = quad.nominalFace();
        final Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(rotate(cullFace, mat));
        }

        quad.nominalFace(rotate(nominalFace, mat));

        return true;
    }

    private Direction rotate(final Direction facing, final Matrix4f mat) {
        final Vec3i dir = facing.getNormal();
        final Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 1.0F);
        mat.transform(vec);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }
}

package com.refinedmods.refinedstorage2.fabric.render.model.baked;

import com.refinedmods.refinedstorage2.fabric.util.BiDirection;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;

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
            tmp.rotate(direction.getQuaternion());
            tmp.add(0.5F, 0.5F, 0.5F);

            quad.pos(i, tmp);

            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.rotate(direction.getQuaternion());
                quad.normal(i, tmp);
            }
        }

        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(direction.rotate(cullFace));
        }

        quad.nominalFace(direction.rotate(nominalFace));

        return true;
    }
}

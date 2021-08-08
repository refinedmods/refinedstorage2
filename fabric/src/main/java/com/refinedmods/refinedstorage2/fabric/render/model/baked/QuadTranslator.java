package com.refinedmods.refinedstorage2.fabric.render.model.baked;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.util.math.Vec3f;

public class QuadTranslator implements RenderContext.QuadTransform {
    private final float x;
    private final float y;
    private final float z;

    public QuadTranslator(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean transform(MutableQuadView quad) {
        Vec3f target = new Vec3f();

        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, target);
            target.add(this.x, this.y, this.z);
            quad.pos(i, target);
        }

        return true;
    }
}

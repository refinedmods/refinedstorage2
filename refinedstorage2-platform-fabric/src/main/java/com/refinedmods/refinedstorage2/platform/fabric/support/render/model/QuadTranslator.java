package com.refinedmods.refinedstorage2.platform.fabric.support.render.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import org.joml.Vector3f;

public class QuadTranslator implements RenderContext.QuadTransform {
    private final float x;
    private final float y;
    private final float z;

    public QuadTranslator(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean transform(final MutableQuadView quad) {
        final Vector3f target = new Vector3f();
        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, target);
            target.add(this.x, this.y, this.z);
            quad.pos(i, target);
        }
        return true;
    }
}

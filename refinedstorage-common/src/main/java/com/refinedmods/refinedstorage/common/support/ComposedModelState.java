package com.refinedmods.refinedstorage.common.support;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import org.joml.Matrix4fc;

public final class ComposedModelState implements ModelState {
    private final ModelState parent;
    private final Transformation transformation;

    public ComposedModelState(final ModelState parent, final Transformation transformation) {
        this.parent = parent;
        this.transformation = parent.transformation().compose(transformation);
    }

    @Override
    public Transformation transformation() {
        return transformation;
    }

    @Override
    public Matrix4fc faceTransformation(final Direction side) {
        return parent.faceTransformation(side);
    }

    @Override
    public Matrix4fc inverseFaceTransformation(final Direction side) {
        return parent.inverseFaceTransformation(side);
    }
}


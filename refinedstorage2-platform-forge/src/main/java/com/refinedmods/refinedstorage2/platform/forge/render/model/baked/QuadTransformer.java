package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;

public final class QuadTransformer {
    private QuadTransformer() {
    }

    public static List<BakedQuad> transformSideAndRotate(Function<Direction, List<BakedQuad>> quadGetter, BiDirection direction, Direction side) {
        Transformation transformation = new Transformation(null, createQuaternion(direction), null, null);

        ImmutableList.Builder<BakedQuad> rotated = ImmutableList.builder();

        for (BakedQuad quad : quadGetter.apply(transformSide(side, transformation.getMatrix()))) {
            BakedQuadBuilder builder = new BakedQuadBuilder(quad.getSprite());
            TRSRTransformer transformer = new TRSRTransformer(builder, transformation.blockCenterToCorner());

            quad.pipe(transformer);

            builder.setQuadOrientation(rotate(quad.getDirection(), transformation.getMatrix()));

            rotated.add(builder.build());
        }

        return rotated.build();
    }

    private static Quaternion createQuaternion(BiDirection direction) {
        return new Quaternion(direction.getVec().x(), direction.getVec().y(), direction.getVec().z(), true);
    }

    private static Direction transformSide(Direction facing, Matrix4f mat) {
        for (Direction face : Direction.values()) {
            if (rotate(face, mat) == facing) {
                return face;
            }
        }
        return null;
    }

    private static Direction rotate(Direction facing, Matrix4f mat) {
        Vec3i dir = facing.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 1);
        vec.transform(mat);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    public static List<BakedQuad> translate(List<BakedQuad> quads, Vector3f translation) {
        Transformation transformation = new Transformation(translation, null, null, null);

        ImmutableList.Builder<BakedQuad> translated = ImmutableList.builder();

        for (BakedQuad quad : quads) {
            BakedQuadBuilder builder = new BakedQuadBuilder(quad.getSprite());
            TRSRTransformer transformer = new TRSRTransformer(builder, transformation.blockCenterToCorner());

            quad.pipe(transformer);

            translated.add(builder.build());
        }

        return translated.build();
    }
}

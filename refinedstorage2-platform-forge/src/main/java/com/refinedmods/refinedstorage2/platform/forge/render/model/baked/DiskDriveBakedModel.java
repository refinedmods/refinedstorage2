package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBakedModel extends ForwardingBakedModel {
    private final BakedModel diskModel;
    private final Vector3f[] translators = new Vector3f[8];

    public DiskDriveBakedModel(BakedModel baseModel, BakedModel diskModel) {
        super(baseModel);
        this.diskModel = diskModel;

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                translators[i++] = new Vector3f(x == 0 ? -(2F / 16F) : -(9F / 16F), -((y * 3F) / 16F) - (2F / 16F), 0);
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        if (state == null) {
            return super.getQuads(state, side, rand);
        }

        List<BakedQuad> quads = new ArrayList<>();

        BiDirection direction = state.getValue(BaseBlock.DIRECTION);

        quads.addAll(QuadTransformer.getQuads(this.baseModel, direction, null, state, rand, side));

        return quads;
    }
}

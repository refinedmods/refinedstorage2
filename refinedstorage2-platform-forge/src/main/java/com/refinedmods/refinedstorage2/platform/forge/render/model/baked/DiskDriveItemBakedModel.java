package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveItemBakedModel extends ForwardingBakedModel {
    private final BakedModel diskDisconnectedModel;
    private final Vector3f[] translators;
    private final long disks;
    private final Map<Direction, List<BakedQuad>> quadCache = new EnumMap<>(Direction.class);
    private List<BakedQuad> cachedQuadsForNoSide;

    public DiskDriveItemBakedModel(BakedModel baseModel, BakedModel diskDisconnectedModel, Vector3f[] translators, long disks) {
        super(baseModel);
        this.diskDisconnectedModel = diskDisconnectedModel;
        this.translators = translators;
        this.disks = disks;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        if (side == null) {
            if (cachedQuadsForNoSide == null) {
                cachedQuadsForNoSide = getDiskModel(null, rand);
            }
            return cachedQuadsForNoSide;
        }
        return quadCache.computeIfAbsent(side, key -> getDiskModel(key, rand));
    }

    private List<BakedQuad> getDiskModel(@Nullable Direction side, RandomSource rand) {
        List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(null, side, rand, EmptyModelData.INSTANCE));
        for (int i = 0; i < translators.length; ++i) {
            if ((disks & (1L << i)) != 0) {
                quads.addAll(getDiskModel(side, rand, translators[i]));
            }
        }
        return quads;
    }

    private List<BakedQuad> getDiskModel(@Nullable Direction side, RandomSource rand, Vector3f translation) {
        List<BakedQuad> diskQuads = diskDisconnectedModel.getQuads(null, side, rand, EmptyModelData.INSTANCE);
        return QuadTransformer.translate(diskQuads, translation);
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return ForgeHooksClient.handlePerspective(this, cameraTransformType, poseStack);
    }
}

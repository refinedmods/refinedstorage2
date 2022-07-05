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

public class DiskDriveItemBakedModel extends AbstractForwardingBakedModel {
    private final BakedModel diskDisconnectedModel;
    private final Vector3f[] translators;
    private final long disks;
    private final Map<Direction, List<BakedQuad>> quadCache = new EnumMap<>(Direction.class);
    @Nullable
    private List<BakedQuad> cachedQuadsForNoSide;

    public DiskDriveItemBakedModel(final BakedModel baseModel,
                                   final BakedModel diskDisconnectedModel,
                                   final Vector3f[] translators,
                                   final long disks) {
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
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    @NotNull final RandomSource rand) {
        if (side == null) {
            if (cachedQuadsForNoSide == null) {
                cachedQuadsForNoSide = getDiskModel(null, rand);
            }
            return cachedQuadsForNoSide;
        }
        return quadCache.computeIfAbsent(side, key -> getDiskModel(key, rand));
    }

    private List<BakedQuad> getDiskModel(@Nullable final Direction side,
                                         final RandomSource rand) {
        final List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(
            null,
            side,
            rand,
            EmptyModelData.INSTANCE
        ));
        for (int i = 0; i < translators.length; ++i) {
            if ((disks & (1L << i)) != 0) {
                quads.addAll(getDiskModel(side, rand, translators[i]));
            }
        }
        return quads;
    }

    private List<BakedQuad> getDiskModel(@Nullable final Direction side,
                                         final RandomSource rand,
                                         final Vector3f translation) {
        final List<BakedQuad> diskQuads = diskDisconnectedModel.getQuads(
            null,
            side,
            rand,
            EmptyModelData.INSTANCE
        );
        return QuadTransformer.translate(diskQuads, translation);
    }

    @Override
    public BakedModel handlePerspective(final ItemTransforms.TransformType cameraTransformType,
                                        final PoseStack poseStack) {
        return ForgeHooksClient.handlePerspective(this, cameraTransformType, poseStack);
    }
}

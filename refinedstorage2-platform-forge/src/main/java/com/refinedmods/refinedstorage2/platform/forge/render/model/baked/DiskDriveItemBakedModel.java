package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.joml.Vector3f;

public class DiskDriveItemBakedModel extends BakedModelWrapper<BakedModel> {
    private final BakedModel baseModel;
    private final Function<Vector3f, BakedModel> diskBakery;
    private final Vector3f[] translators;
    private final long disks;
    private final Map<Direction, List<BakedQuad>> cache = new EnumMap<>(Direction.class);
    @Nullable
    private List<BakedQuad> noSideCache;

    public DiskDriveItemBakedModel(final BakedModel baseModel,
                                   final Function<Vector3f, BakedModel> diskBakery,
                                   final Vector3f[] translators,
                                   final long disks) {
        super(baseModel);
        this.baseModel = baseModel;
        this.diskBakery = diskBakery;
        this.translators = translators;
        this.disks = disks;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    @Nonnull final RandomSource rand) {
        if (side == null) {
            if (noSideCache == null) {
                noSideCache = createQuads(state, null, rand);
            }
            return noSideCache;
        }
        return cache.computeIfAbsent(side, key -> createQuads(state, side, rand));
    }

    @SuppressWarnings("deprecation")
    private List<BakedQuad> createQuads(@Nullable final BlockState state,
                                        @Nullable final Direction side,
                                        @Nonnull final RandomSource rand) {
        final List<BakedQuad> quads = new ArrayList<>(super.getQuads(
            state,
            side,
            rand
        ));
        for (int i = 0; i < translators.length; ++i) {
            if ((disks & (1L << i)) != 0) {
                quads.addAll(diskBakery.apply(translators[i]).getQuads(state, side, rand));
            }
        }
        return quads;
    }

    @Override
    public List<BakedModel> getRenderPasses(final ItemStack itemStack, final boolean fabulous) {
        return List.of(this);
    }

    @Override
    public BakedModel applyTransform(final ItemDisplayContext cameraTransformType,
                                     final PoseStack poseStack,
                                     final boolean applyLeftHandTransform) {
        baseModel.getTransforms().getTransform(cameraTransformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }
}

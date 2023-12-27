package com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive;

import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.joml.Vector3f;

class DiskDriveItemBakedModel extends BakedModelWrapper<BakedModel> {
    private final BakedModel baseModel;
    private final Map<Item, Function<Vector3f, BakedModel>> diskItemModelBakeries;
    private final Function<Vector3f, BakedModel> inactiveLedBakery;
    private final Vector3f[] translators;
    private final Map<Direction, List<BakedQuad>> cache = new EnumMap<>(Direction.class);
    @Nullable
    private List<BakedQuad> noSideCache;
    private final List<Item> disks;

    DiskDriveItemBakedModel(final BakedModel baseModel,
                            final Map<Item, Function<Vector3f, BakedModel>> diskItemModelBakeries,
                            final Function<Vector3f, BakedModel> inactiveLedBakery,
                            final Vector3f[] translators,
                            final List<Item> disks) {
        super(baseModel);
        this.baseModel = baseModel;
        this.diskItemModelBakeries = diskItemModelBakeries;
        this.inactiveLedBakery = inactiveLedBakery;
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
                                        final RandomSource rand) {
        final List<BakedQuad> quads = new ArrayList<>(super.getQuads(
            state,
            side,
            rand
        ));
        for (int i = 0; i < translators.length; ++i) {
            final Item disk = disks.get(i);
            final List<BakedQuad> diskQuads = getDiskQuads(state, side, rand, disk, i);
            quads.addAll(diskQuads);
            quads.addAll(inactiveLedBakery.apply(translators[i]).getQuads(state, side, rand));
        }
        return quads;
    }

    @SuppressWarnings("deprecation")
    private List<BakedQuad> getDiskQuads(@Nullable final BlockState state,
                                         @Nullable final Direction side,
                                         final RandomSource rand,
                                         @Nullable final Item diskItem,
                                         final int i) {
        if (diskItem == null) {
            return Collections.emptyList();
        }
        final Function<Vector3f, BakedModel> diskModelBakery = diskItemModelBakeries.get(diskItem);
        if (diskModelBakery == null) {
            return Collections.emptyList();
        }
        return diskModelBakery.apply(translators[i]).getQuads(state, side, rand);
    }

    @Override
    public List<BakedModel> getRenderPasses(final ItemStack itemStack, final boolean fabulous) {
        return List.of(this);
    }

    @Override
    public BakedModel applyTransform(final ItemDisplayContext cameraTransformType,
                                     final PoseStack poseStack,
                                     final boolean applyLeftHandTransform) {
        baseModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
        return this;
    }
}

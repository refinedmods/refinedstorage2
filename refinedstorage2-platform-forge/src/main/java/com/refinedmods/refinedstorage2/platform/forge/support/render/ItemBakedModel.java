package com.refinedmods.refinedstorage2.platform.forge.support.render;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

public class ItemBakedModel extends BakedModelWrapper<BakedModel> {
    private final List<BakedQuad> unculledFaces;
    private final Map<Direction, List<BakedQuad>> faces;

    public ItemBakedModel(final BakedModel originalModel,
                          final List<BakedQuad> unculledFaces,
                          final Map<Direction, List<BakedQuad>> faces) {
        super(originalModel);
        this.unculledFaces = unculledFaces;
        this.faces = faces;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    final RandomSource rand) {
        return side == null ? unculledFaces : faces.getOrDefault(side, Collections.emptyList());
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public List<BakedModel> getRenderPasses(final ItemStack itemStack, final boolean fabulous) {
        return List.of(this);
    }

    @Override
    public BakedModel applyTransform(final ItemDisplayContext cameraTransformType,
                                     final PoseStack poseStack,
                                     final boolean applyLeftHandTransform) {
        originalModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
        return this;
    }
}

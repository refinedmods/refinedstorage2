package com.refinedmods.refinedstorage2.platform.forge.support.render;

import java.util.function.Function;
import javax.annotation.Nullable;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;

public class RotationTranslationModelBaker {
    private final ModelState state;
    private final ModelBaker baker;
    private final Function<Material, TextureAtlasSprite> spriterGetter;
    private final ResourceLocation model;

    public RotationTranslationModelBaker(final ModelState state,
                                         final ModelBaker baker,
                                         final Function<Material, TextureAtlasSprite> spriterGetter,
                                         final ResourceLocation model) {
        this.state = state;
        this.baker = baker;
        this.spriterGetter = spriterGetter;
        this.model = model;
    }

    @Nullable
    public BakedModel bake(final Transformation transformation) {
        final ModelState wrappedState = new SimpleModelState(transformation, state.isUvLocked());
        return baker.bake(model, wrappedState, spriterGetter);
    }
}

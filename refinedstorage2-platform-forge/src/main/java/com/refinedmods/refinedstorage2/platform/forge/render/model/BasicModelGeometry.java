package com.refinedmods.refinedstorage2.platform.forge.render.model;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public abstract class BasicModelGeometry<T extends IModelGeometry<T>> implements IModelGeometry<T> {
    protected abstract Set<ResourceLocation> getModels();

    @Override
    public Collection<Material> getTextures(final IModelConfiguration owner,
                                            final Function<ResourceLocation, UnbakedModel> modelGetter,
                                            final Set<Pair<String, String>> missingTextureErrors) {
        return getModels()
                .stream()
                .map(modelGetter)
                .flatMap(unbakedModel -> unbakedModel.getMaterials(modelGetter, missingTextureErrors).stream())
                .toList();
    }
}

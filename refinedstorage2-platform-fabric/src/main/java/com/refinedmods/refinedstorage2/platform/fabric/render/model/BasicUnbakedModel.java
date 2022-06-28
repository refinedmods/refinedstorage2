package com.refinedmods.refinedstorage2.platform.fabric.render.model;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public interface BasicUnbakedModel extends UnbakedModel {
    @Override
    default Collection<Material> getMaterials(final Function<ResourceLocation, UnbakedModel> unbakedModelGetter,
                                              final Set<Pair<String, String>> unresolvedTextureReferences) {
        return getDependencies()
                .stream()
                .map(unbakedModelGetter)
                .flatMap(unbakedModel -> unbakedModel.getMaterials(unbakedModelGetter, unresolvedTextureReferences)
                        .stream())
                .toList();
    }
}

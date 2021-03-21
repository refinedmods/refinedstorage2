package com.refinedmods.refinedstorage2.fabric.render.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface BasicUnbakedModel extends UnbakedModel {
    @Override
    default Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return getModelDependencies()
            .stream()
            .map(unbakedModelGetter)
            .flatMap(unbakedModel -> unbakedModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream())
            .collect(Collectors.toList());
    }
}
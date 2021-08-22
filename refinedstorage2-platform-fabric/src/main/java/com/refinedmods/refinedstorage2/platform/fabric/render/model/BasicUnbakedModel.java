package com.refinedmods.refinedstorage2.platform.fabric.render.model;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public interface BasicUnbakedModel extends UnbakedModel {
    @Override
    default Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return getModelDependencies()
                .stream()
                .map(unbakedModelGetter)
                .flatMap(unbakedModel -> unbakedModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream())
                .toList();
    }
}

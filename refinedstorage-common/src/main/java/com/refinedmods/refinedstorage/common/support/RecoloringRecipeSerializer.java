package com.refinedmods.refinedstorage.common.support;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecoloringRecipeSerializer implements RecipeSerializer<RecoloringRecipe> {
    public static final MapCodec<RecoloringRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(RecoloringRecipe::getIngredient),
            Ingredient.CODEC.fieldOf("dye").forGetter(RecoloringRecipe::getDye),
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("result").forGetter(RecoloringRecipe::getResult)
        ).apply(instance, RecoloringRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecoloringRecipe> STREAM_CODEC =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecoloringRecipe::getIngredient,
            Ingredient.CONTENTS_STREAM_CODEC, RecoloringRecipe::getDye,
            ByteBufCodecs.holderRegistry(Registries.ITEM), RecoloringRecipe::getResult,
            RecoloringRecipe::new
        );

    @Override
    public MapCodec<RecoloringRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecoloringRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

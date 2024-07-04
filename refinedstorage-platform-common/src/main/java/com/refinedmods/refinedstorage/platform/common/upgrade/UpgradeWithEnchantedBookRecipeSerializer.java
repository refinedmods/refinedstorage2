package com.refinedmods.refinedstorage.platform.common.upgrade;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class UpgradeWithEnchantedBookRecipeSerializer implements RecipeSerializer<UpgradeWithEnchantedBookRecipe> {
    @Override
    public MapCodec<UpgradeWithEnchantedBookRecipe> codec() {
        return UpgradeWithEnchantedBookRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UpgradeWithEnchantedBookRecipe> streamCodec() {
        return UpgradeWithEnchantedBookRecipe.STREAM_CODEC;
    }
}

package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class UpgradeWithEnchantedBookRecipeSerializer implements RecipeSerializer<UpgradeWithEnchantedBookRecipe> {
    @Override
    public Codec<UpgradeWithEnchantedBookRecipe> codec() {
        return UpgradeWithEnchantedBookRecipe.CODEC;
    }

    @Override
    public UpgradeWithEnchantedBookRecipe fromNetwork(final FriendlyByteBuf buf) {
        final ResourceLocation resultItemId = buf.readResourceLocation();
        final ResourceLocation enchantmentId = buf.readResourceLocation();
        final int level = buf.readInt();
        return new UpgradeWithEnchantedBookRecipe(enchantmentId, level, resultItemId);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buf, final UpgradeWithEnchantedBookRecipe recipe) {
        buf.writeResourceLocation(recipe.getResultItemId());
        buf.writeResourceLocation(recipe.getEnchantmentId());
        buf.writeInt(recipe.getEnchantmentLevel());
    }
}

package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.RecipeSerializers;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class UpgradeWithEnchantedBookRecipe extends ShapedRecipe {
    public static final MapCodec<UpgradeWithEnchantedBookRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment")
                .forGetter(UpgradeWithEnchantedBookRecipe::getEnchantment),
            Codec.INT.fieldOf("level").orElse(1)
                .forGetter(UpgradeWithEnchantedBookRecipe::getEnchantmentLevel),
            ItemStackTemplate.CODEC.fieldOf("result")
                .forGetter(UpgradeWithEnchantedBookRecipe::getResultItem)
        ).apply(instance, UpgradeWithEnchantedBookRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeWithEnchantedBookRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT), UpgradeWithEnchantedBookRecipe::getEnchantment,
            ByteBufCodecs.INT, UpgradeWithEnchantedBookRecipe::getEnchantmentLevel,
            ItemStackTemplate.STREAM_CODEC, UpgradeWithEnchantedBookRecipe::getResultItem,
            UpgradeWithEnchantedBookRecipe::new
        );

    private final Holder<Enchantment> enchantment;
    private final int level;
    private final ItemStackTemplate resultItem;

    public UpgradeWithEnchantedBookRecipe(final Holder<Enchantment> enchantment,
                                          final int level,
                                          final ItemStackTemplate resultItem) {
        super(
            new Recipe.CommonInfo(false),
            new CraftingBookInfo(CraftingBookCategory.MISC, ""),
            new ShapedRecipePattern(3, 3, List.of(
                Optional.of(Ingredient.of(Items.INSTANCE.getQuartzEnrichedIron())),
                Optional.of(Ingredient.of(net.minecraft.world.item.Items.ENCHANTED_BOOK)),
                Optional.of(Ingredient.of(Items.INSTANCE.getQuartzEnrichedIron())),
                Optional.of(Ingredient.of(Blocks.BOOKSHELF)),
                Optional.of(Ingredient.of(Items.INSTANCE.getUpgrade())),
                Optional.of(Ingredient.of(Blocks.BOOKSHELF)),
                Optional.of(Ingredient.of(Items.INSTANCE.getQuartzEnrichedIron())),
                Optional.of(Ingredient.of(Items.INSTANCE.getQuartzEnrichedIron())),
                Optional.of(Ingredient.of(Items.INSTANCE.getQuartzEnrichedIron()))
            ), Optional.empty()),
            resultItem
        );
        this.enchantment = enchantment;
        this.level = level;
        this.resultItem = resultItem;
    }

    ItemStackTemplate getResultItem() {
        return resultItem;
    }

    Holder<Enchantment> getEnchantment() {
        return enchantment;
    }

    int getEnchantmentLevel() {
        return level;
    }

    @Override
    public boolean matches(final CraftingInput craftingContainer, final Level theLevel) {
        if (!super.matches(craftingContainer, theLevel)) {
            return false;
        }
        final ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(
            craftingContainer.getItem(1)
        );
        return enchantments.getLevel(enchantment) == level;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) RecipeSerializers.INSTANCE.getUpgradeWithEnchantedBook();
    }
}

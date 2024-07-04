package com.refinedmods.refinedstorage.platform.common.upgrade;

import com.refinedmods.refinedstorage.platform.common.content.Items;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
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
            ItemStack.CODEC.fieldOf("result")
                .forGetter(UpgradeWithEnchantedBookRecipe::getResultItem)
        ).apply(instance, UpgradeWithEnchantedBookRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeWithEnchantedBookRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT), UpgradeWithEnchantedBookRecipe::getEnchantment,
            ByteBufCodecs.INT, UpgradeWithEnchantedBookRecipe::getEnchantmentLevel,
            ItemStack.STREAM_CODEC, UpgradeWithEnchantedBookRecipe::getResultItem,
            UpgradeWithEnchantedBookRecipe::new
        );

    private final Holder<Enchantment> enchantment;
    private final int level;
    private final ItemStack resultItem;

    UpgradeWithEnchantedBookRecipe(final Holder<Enchantment> enchantment,
                                   final int level,
                                   final ItemStack resultItem) {
        super("", CraftingBookCategory.MISC, new ShapedRecipePattern(3, 3, NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level))),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getUpgrade())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron()))
        ), Optional.empty()), resultItem);
        this.enchantment = enchantment;
        this.level = level;
        this.resultItem = resultItem;
    }

    ItemStack getResultItem() {
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
}

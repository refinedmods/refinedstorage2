package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import static java.util.Objects.requireNonNull;

public class UpgradeWithEnchantedBookRecipe extends ShapedRecipe {
    public static final Codec<UpgradeWithEnchantedBookRecipe> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("enchantment")
                .xmap(ResourceLocation::new, ResourceLocation::toString)
                .forGetter(UpgradeWithEnchantedBookRecipe::getEnchantmentId),
            Codec.INT.fieldOf("level").orElse(1)
                .forGetter(UpgradeWithEnchantedBookRecipe::getEnchantmentLevel),
            Codec.STRING.fieldOf("result")
                .xmap(ResourceLocation::new, ResourceLocation::toString)
                .forGetter(UpgradeWithEnchantedBookRecipe::getResultItemId)
        ).apply(instance, UpgradeWithEnchantedBookRecipe::new)
    );

    private final ResourceLocation enchantmentId;
    private final int level;
    private final ResourceLocation resultItemId;

    UpgradeWithEnchantedBookRecipe(final ResourceLocation enchantmentId,
                                   final int level,
                                   final ResourceLocation resultItemId) {
        super("", CraftingBookCategory.MISC, new ShapedRecipePattern(3, 3, NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                getEnchantment(enchantmentId),
                level
            ))),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getUpgrade())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron()))
        ), Optional.empty()), new ItemStack(BuiltInRegistries.ITEM.get(resultItemId)));
        this.enchantmentId = enchantmentId;
        this.level = level;
        this.resultItemId = resultItemId;
    }

    private static Enchantment getEnchantment(final ResourceLocation enchantmentId) {
        return requireNonNull(BuiltInRegistries.ENCHANTMENT.get(enchantmentId));
    }

    ResourceLocation getResultItemId() {
        return resultItemId;
    }

    ResourceLocation getEnchantmentId() {
        return enchantmentId;
    }

    int getEnchantmentLevel() {
        return level;
    }

    @Override
    public boolean matches(final CraftingContainer craftingContainer, final Level theLevel) {
        if (!super.matches(craftingContainer, theLevel)) {
            return false;
        }
        final ListTag enchantments = EnchantedBookItem.getEnchantments(craftingContainer.getItem(1));
        for (int i = 0; i < enchantments.size(); ++i) {
            final CompoundTag tag = enchantments.getCompound(i);
            final int containerLevel = EnchantmentHelper.getEnchantmentLevel(tag);
            final ResourceLocation containerEnchantment = EnchantmentHelper.getEnchantmentId(tag);
            if (Objects.equals(containerEnchantment, getEnchantmentId()) && containerLevel == level) {
                return true;
            }
        }
        return false;
    }
}

package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.Objects;
import javax.annotation.Nullable;

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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class UpgradeWithEnchantedBookRecipe extends ShapedRecipe {
    private final EnchantmentInstance enchantment;
    private final ItemStack theResult;

    UpgradeWithEnchantedBookRecipe(final ResourceLocation recipeId,
                                   final Enchantment enchantment,
                                   final int enchantmentLevel,
                                   final ItemStack theResult) {
        super(recipeId, "", CraftingBookCategory.MISC, 3, 3, NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(enchantment, enchantmentLevel)
            )),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getUpgrade())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron()))
        ), theResult);
        this.enchantment = new EnchantmentInstance(enchantment, enchantmentLevel);
        this.theResult = theResult;
    }

    public ItemStack getResult() {
        return theResult;
    }

    @Nullable
    @SuppressWarnings("deprecation") // Forge deprecates BuiltinRegistries
    public ResourceLocation getEnchantmentId() {
        return BuiltInRegistries.ENCHANTMENT.getKey(enchantment.enchantment);
    }

    public int getEnchantmentLevel() {
        return enchantment.level;
    }

    @Override
    public boolean matches(final CraftingContainer craftingContainer, final Level level) {
        if (!super.matches(craftingContainer, level)) {
            return false;
        }
        final ListTag enchantments = EnchantedBookItem.getEnchantments(craftingContainer.getItem(1));
        for (int i = 0; i < enchantments.size(); ++i) {
            final CompoundTag tag = enchantments.getCompound(i);
            final int lvl = EnchantmentHelper.getEnchantmentLevel(tag);
            final ResourceLocation enchantmentId = EnchantmentHelper.getEnchantmentId(tag);
            if (Objects.equals(enchantmentId, getEnchantmentId()) && lvl == enchantment.level) {
                return true;
            }
        }
        return false;
    }
}

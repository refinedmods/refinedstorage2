package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.common.content.RecipeSerializers;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

public class StorageContainerUpgradeRecipe extends ShapelessRecipe {
    public static final MapCodec<StorageContainerUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.list(Item.CODEC).fieldOf("sources").forGetter(StorageContainerUpgradeRecipe::getSources),
            Item.CODEC.fieldOf("part").forGetter(StorageContainerUpgradeRecipe::getPart),
            Item.CODEC.fieldOf("result").forGetter(StorageContainerUpgradeRecipe::getResult)
        ).apply(instance, StorageContainerUpgradeRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageContainerUpgradeRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.holderRegistry(Registries.ITEM)),
            StorageContainerUpgradeRecipe::getSources,
            ByteBufCodecs.holderRegistry(Registries.ITEM), StorageContainerUpgradeRecipe::getPart,
            ByteBufCodecs.holderRegistry(Registries.ITEM), StorageContainerUpgradeRecipe::getResult,
            StorageContainerUpgradeRecipe::new
        );

    private final List<Holder<Item>> sources;
    private final Holder<Item> part;
    private final Holder<Item> result;

    public StorageContainerUpgradeRecipe(final List<Holder<Item>> sources, final Holder<Item> part,
                                         final Holder<Item> result) {
        super(
            new CommonInfo(false),
            new CraftingBookInfo(CraftingBookCategory.MISC, ""),
            new ItemStackTemplate(result.value()),
            getIngredients(sources.stream().map(Holder::value).toList(), part.value())
        );
        this.sources = sources;
        this.part = part;
        this.result = result;
    }

    public List<Holder<Item>> getSources() {
        return sources;
    }

    public Holder<Item> getPart() {
        return part;
    }

    public Holder<Item> getResult() {
        return result;
    }

    private static NonNullList<Ingredient> getIngredients(final List<Item> sources, final Item part) {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(sources.stream()));
        ingredients.add(Ingredient.of(part));
        return ingredients;
    }

    @Override
    public ItemStack assemble(final CraftingInput input) {
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack fromDisk = input.getItem(i);
            if (fromDisk.getItem() instanceof UpgradeableStorageContainer upgrader
                && sources.contains(fromDisk.typeHolder())) {
                final ItemStack toDisk = super.assemble(input);
                upgrader.transferTo(fromDisk, toDisk);
                return toDisk;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        int validSources = 0;
        int validParts = 0;
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack inputStack = input.getItem(i);
            if (sources.contains(inputStack.typeHolder())) {
                validSources++;
            } else if (inputStack.typeHolder() == part) {
                validParts++;
            }
        }
        return validParts == 1 && validSources == 1;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput input) {
        final NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof UpgradeableStorageContainer upgrader
                && sources.contains(stack.typeHolder())) {
                final Item sourceStoragePart = upgrader.getVariant().getStoragePart();
                if (sourceStoragePart != null) {
                    remainingItems.set(i, sourceStoragePart.getDefaultInstance());
                }
            }
        }
        return remainingItems;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer) RecipeSerializers.INSTANCE.getStorageContainerUpgrade();
    }
}

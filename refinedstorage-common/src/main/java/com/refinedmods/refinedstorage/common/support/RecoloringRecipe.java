package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.content.RecipeSerializers;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

public class RecoloringRecipe extends ShapelessRecipe {
    public static final MapCodec<RecoloringRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(RecoloringRecipe::getIngredient),
            new ColorSetCodec().fieldOf("color").forGetter(RecoloringRecipe::getDyes),
            Item.CODEC.fieldOf("result").forGetter(RecoloringRecipe::getResult)
        ).apply(instance, RecoloringRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecoloringRecipe> STREAM_CODEC =
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecoloringRecipe::getIngredient,
            ByteBufCodecs.holderSet(Registries.ITEM), RecoloringRecipe::getDyes,
            ByteBufCodecs.holderRegistry(Registries.ITEM), RecoloringRecipe::getResult,
            RecoloringRecipe::new
        );

    private final Ingredient ingredient;
    private final HolderSet<Item> dyes;
    private final Holder<Item> result;

    private RecoloringRecipe(final Ingredient ingredient, final HolderSet<Item> dyes, final Holder<Item> result) {
        super(
            new CommonInfo(false),
            new CraftingBookInfo(CraftingBookCategory.MISC, ""),
            new ItemStackTemplate(result.value()),
            getIngredients(ingredient, dyes)
        );
        this.ingredient = ingredient;
        this.dyes = dyes;
        this.result = result;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public HolderSet<Item> getDyes() {
        return dyes;
    }

    public Holder<Item> getResult() {
        return result;
    }

    private static NonNullList<Ingredient> getIngredients(final Ingredient ingredient, final HolderSet<Item> dyes) {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        ingredients.add(Ingredient.of(dyes));
        return ingredients;
    }

    @Override
    public ItemStack assemble(final CraftingInput input) {
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (ingredient.test(stack)) {
                final ItemStack copied = result.value().getDefaultInstance();
                copied.copyFrom(DataComponents.BLOCK_ENTITY_DATA, stack);
                return copied;
            }
        }
        return result.value().getDefaultInstance();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer) RecipeSerializers.INSTANCE.getRecoloring();
    }

    @SuppressWarnings("deprecation")
    public static RecoloringRecipe create(
        final TagKey<Item> ingredient,
        final DyeColor color,
        final Block result,
        final HolderLookup.Provider registries
    ) {
        return new RecoloringRecipe(
            Ingredient.of(registries.getOrThrow(ingredient)),
            registries.getOrThrow(createTag(color)),
            result.asItem().builtInRegistryHolder()
        );
    }

    private static TagKey<Item> createTag(final DyeColor color) {
        return TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "dyes/" + color.getSerializedName())
        );
    }

    private static final class ColorSetCodec implements Codec<HolderSet<Item>> {
        private final Map<TagKey<Item>, DyeColor> colorByTag = new HashMap<>();
        private final Map<DyeColor, TagKey<Item>> tagByColor = new EnumMap<>(DyeColor.class);

        private ColorSetCodec() {
            for (final DyeColor color : DyeColor.values()) {
                final TagKey<Item> tag = createTag(color);
                colorByTag.put(tag, color);
                tagByColor.put(color, tag);
            }
        }

        @Override
        public <T> DataResult<Pair<HolderSet<Item>, T>> decode(final DynamicOps<T> ops, final T input) {
            if (!(ops instanceof RegistryOps<T> registries)) {
                return DataResult.error(() -> "Cannot decode without registries");
            }
            return DyeColor.CODEC.decode(ops, input).flatMap(
                pair -> registries.getter(Registries.ITEM).map(lookup -> lookup.get(tagByColor.get(pair.getFirst()))
                        .map(holders -> DataResult.success(Pair.of((HolderSet<Item>) holders, pair.getSecond())))
                        .orElseGet(() -> DataResult.error(() -> "Cannot access item registry")))
                    .orElseGet(() -> DataResult.error(() -> "Cannot access item registry")));
        }

        @Override
        public <T> DataResult<T> encode(final HolderSet<Item> input, final DynamicOps<T> ops, final T prefix) {
            final Either<TagKey<Item>, List<Holder<Item>>> either = input.unwrap();
            return either.left().map(
                left -> DyeColor.CODEC.encode(colorByTag.get(left), ops, prefix)
            ).orElseGet(() -> DataResult.error(() -> "Cannot encode direct holder set as color"));
        }
    }
}

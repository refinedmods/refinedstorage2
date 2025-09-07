package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec.ERROR_MESSAGE_PATTERN;

public record ProcessingPatternState(
    List<Optional<ProcessingIngredient>> ingredients,
    List<Optional<ResourceAmount>> outputs
) {
    public static final Codec<ProcessingPatternState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        new ErrorHandlingListCodec<>(ProcessingIngredient.OPTIONAL_CODEC, ERROR_MESSAGE_PATTERN).fieldOf("ingredients")
            .forGetter(ProcessingPatternState::ingredients),
        new ErrorHandlingListCodec<>(ResourceCodecs.AMOUNT_OPTIONAL_CODEC, ERROR_MESSAGE_PATTERN).fieldOf("outputs")
            .forGetter(ProcessingPatternState::outputs)
    ).apply(instance, ProcessingPatternState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingPatternState> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ProcessingIngredient.OPTIONAL_STREAM_CODEC),
            ProcessingPatternState::ingredients,
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_OPTIONAL_CODEC),
            ProcessingPatternState::outputs,
            ProcessingPatternState::new
        );

    List<Ingredient> getIngredients() {
        return ingredients
            .stream()
            .flatMap(ingredient -> ingredient.map(ProcessingIngredient::toIngredient).stream())
            .toList();
    }

    List<ResourceAmount> getFlatInputs() {
        final MutableResourceList list = MutableResourceListImpl.orderPreserving();
        ingredients.forEach(ingredient -> ingredient.map(ProcessingIngredient::input).ifPresent(list::add));
        return new ArrayList<>(list.copyState());
    }

    List<ResourceAmount> getFlatOutputs() {
        final MutableResourceList list = MutableResourceListImpl.orderPreserving();
        outputs.forEach(output -> output.ifPresent(list::add));
        return new ArrayList<>(list.copyState());
    }

    public record ProcessingIngredient(ResourceAmount input, List<ResourceLocation> allowedAlternativeIds) {
        public static final Codec<ProcessingIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceCodecs.AMOUNT_CODEC.fieldOf("input")
                .forGetter(ProcessingIngredient::input),
            Codec.list(ResourceLocation.CODEC).fieldOf("allowedAlternativeIds")
                .forGetter(ProcessingIngredient::allowedAlternativeIds)
        ).apply(instance, ProcessingIngredient::new));
        public static final Codec<Optional<ProcessingIngredient>> OPTIONAL_CODEC =
            CODEC.optionalFieldOf("input").codec();

        public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingIngredient> STREAM_CODEC =
            StreamCodec.composite(
                ResourceCodecs.AMOUNT_STREAM_CODEC,
                ProcessingIngredient::input,
                ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
                ProcessingIngredient::allowedAlternativeIds,
                ProcessingIngredient::new
            );
        public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ProcessingIngredient>> OPTIONAL_STREAM_CODEC =
            ByteBufCodecs.optional(STREAM_CODEC);

        public Ingredient toIngredient() {
            return new Ingredient(input.amount(), calculateInputsIncludingAlternatives());
        }

        public List<ResourceKey> calculateInputsIncludingAlternatives() {
            return Stream.concat(
                Stream.of(input.resource()),
                allowedAlternativeIds.stream()
                    .filter(id -> input.resource() instanceof PlatformResourceKey)
                    .map(id -> (PlatformResourceKey) input.resource())
                    .flatMap(resource -> resource.getTags().stream()
                        .filter(tag -> allowedAlternativeIds.contains(tag.key().location()))
                        .flatMap(tag -> tag.resources().stream()))
            ).toList();
        }
    }
}

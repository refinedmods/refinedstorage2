package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public class PatternBuilder {
    private final PatternType type;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<ResourceAmount> outputs = new ArrayList<>();
    private final List<ResourceAmount> byproducts = new ArrayList<>();

    private PatternBuilder(final PatternType type) {
        this.type = type;
    }

    public static PatternBuilder pattern() {
        return pattern(PatternType.INTERNAL);
    }

    public static PatternBuilder pattern(final PatternType type) {
        return new PatternBuilder(type);
    }

    public IngredientBuilder ingredient(final long amount) {
        return new IngredientBuilder(amount);
    }

    public PatternBuilder ingredient(final ResourceKey input, final long amount) {
        ingredients.add(new Ingredient(amount, List.of(input)));
        return this;
    }

    public PatternBuilder output(final ResourceKey output, final long amount) {
        outputs.add(new ResourceAmount(output, amount));
        return this;
    }

    public PatternBuilder byproduct(final ResourceKey byproduct, final long amount) {
        if (type == PatternType.EXTERNAL) {
            throw new IllegalStateException("External patterns cannot have byproducts");
        }
        byproducts.add(new ResourceAmount(byproduct, amount));
        return this;
    }

    public PatternLayout buildLayout() {
        return type == PatternType.INTERNAL
            ? PatternLayout.internal(ingredients, outputs, byproducts)
            : PatternLayout.external(ingredients, outputs);
    }

    public Pattern build() {
        return new Pattern(UUID.randomUUID(), buildLayout());
    }

    public class IngredientBuilder {
        private final long amount;
        private final List<ResourceKey> inputs = new ArrayList<>();

        private IngredientBuilder(final long amount) {
            this.amount = amount;
        }

        public IngredientBuilder input(final ResourceKey input) {
            inputs.add(input);
            return this;
        }

        public PatternBuilder end() {
            ingredients.add(new Ingredient(amount, inputs));
            return PatternBuilder.this;
        }
    }
}

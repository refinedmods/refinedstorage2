package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Concrete recipe representation for the Java LP/autocrafting prototype.
 * <p>This intentionally models only concrete ingredient choices.</p>
 */
public class LpPatternRecipe {
    private final UUID uniqueId;
    private final Pattern pattern;
    private final LpResourceSet input;
    private final LpResourceSet output;
    private final int basePriority;
    private Integer effectivePriority;

    public LpPatternRecipe(final Pattern pattern,
                           final LpResourceSet input,
                           final LpResourceSet output,
                           final int basePriority,
                           final Integer effectivePriority) {
        this.pattern = Objects.requireNonNull(pattern, "pattern cannot be null");
        this.uniqueId = pattern.id();
        this.input = Objects.requireNonNull(input, "input cannot be null").copy();
        this.output = Objects.requireNonNull(output, "output cannot be null").copy();
        this.basePriority = basePriority;
        this.effectivePriority = effectivePriority;
    }

    public static LpPatternRecipe fromPattern(final Pattern pattern, final int basePriority) {
        Objects.requireNonNull(pattern, "pattern cannot be null");

        final LpResourceSet input = new LpResourceSet();
        for (final Ingredient ingredient : pattern.layout().ingredients()) {
            if (ingredient.inputs().size() != 1) {
                throw new IllegalArgumentException(
                    "LP prototype recipes require concrete inputs; pattern " + pattern.id() + " has a fuzzy ingredient"
                );
            }
            input.addAmount(ingredient.inputs().getFirst(), ingredient.amount());
        }

        final LpResourceSet output = new LpResourceSet();
        for (final ResourceAmount resourceAmount : pattern.layout().outputs()) {
            output.addAmount(resourceAmount.resource(), resourceAmount.amount());
        }
        for (final ResourceAmount resourceAmount : pattern.layout().byproducts()) {
            output.addAmount(resourceAmount.resource(), resourceAmount.amount());
        }

        return new LpPatternRecipe(pattern, input, output, basePriority, null);
    }

    public UUID uniqueId() {
        return uniqueId;
    }

    public Pattern pattern() {
        return pattern;
    }

    public LpResourceSet input() {
        return input.copy();
    }

    public LpResourceSet output() {
        return output.copy();
    }

    public int basePriority() {
        return basePriority;
    }

    public Integer effectivePriority() {
        return effectivePriority;
    }

    public void setEffectivePriority(final Integer effectivePriority) {
        this.effectivePriority = effectivePriority;
    }

    public boolean produces(final ResourceKey resource) {
        return output.getAmount(resource) > 0;
    }

    public boolean consumes(final ResourceKey resource) {
        return input.getAmount(resource) > 0;
    }

    public long coefficient(final ResourceKey resource) {
        return output.getAmount(resource) - input.getAmount(resource);
    }

    public LpPatternRecipe copy() {
        return new LpPatternRecipe(pattern, input, output, basePriority, effectivePriority);
    }

    public String description() {
        return describe(input) + " -> " + describe(output);
    }

    private static String describe(final LpResourceSet resources) {
        final StringJoiner joiner = new StringJoiner(" + ");
        resources.asMap().forEach((resource, amount) -> joiner.add(resource + " x" + amount));
        return joiner.length() == 0 ? "<empty>" : joiner.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof LpPatternRecipe other && uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return description();
    }
}

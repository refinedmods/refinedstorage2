package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpPatternRecipeTest {
    @Test
    void fromPatternShouldRejectNullPattern() {
        // Tests that fromPattern rejects null pattern arguments.
        assertThatThrownBy(() -> LpPatternRecipe.fromPattern(null, 0))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("pattern cannot be null");
    }

    @Test
    void fromPatternShouldRejectFuzzyIngredient() {
        // Tests that fromPattern rejects patterns with fuzzy ingredients (multiple possible inputs).
        final Pattern pattern = PatternBuilder.pattern()
            .ingredient(1)
            .input(A)
            .input(B)
            .end()
            .output(C, 1)
            .build();

        assertThatThrownBy(() -> LpPatternRecipe.fromPattern(pattern, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fuzzy ingredient");
    }

    @Test
    void fromPatternShouldIncludeByproductsInOutput() {
        // Tests that byproducts from patterns are included in the output resource set.
        final Pattern pattern = PatternBuilder.pattern()
            .ingredient(A, 1)
            .output(B, 2)
            .byproduct(C, 3)
            .build();

        final LpPatternRecipe sut = LpPatternRecipe.fromPattern(pattern, 7);

        assertThat(sut.basePriority()).isEqualTo(7);
        assertThat(sut.produces(B)).isTrue();
        assertThat(sut.produces(C)).isTrue();
        assertThat(sut.output().getAmount(B)).isEqualTo(2);
        assertThat(sut.output().getAmount(C)).isEqualTo(3);
    }

    @Test
    void gettersShouldReturnDefensiveCopies() {
        // Tests that input and output getters return defensive copies to prevent external mutation.
        final LpPatternRecipe sut = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 2).output(B, 4).build(),
            0
        );

        final LpResourceSet input = sut.input();
        final LpResourceSet output = sut.output();

        input.addAmount(A, 10);
        output.addAmount(B, 5);

        assertThat(sut.input().getAmount(A)).isEqualTo(2);
        assertThat(sut.output().getAmount(B)).isEqualTo(4);
    }

    @Test
    void shouldCalculateConsumesProducesAndCoefficient() {
        // Tests that consumption, production, and coefficient methods correctly report recipe resource transformations.
        final LpPatternRecipe sut = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 3).output(B, 5).build(),
            0
        );

        assertThat(sut.consumes(A)).isTrue();
        assertThat(sut.consumes(B)).isFalse();
        assertThat(sut.produces(B)).isTrue();
        assertThat(sut.produces(A)).isFalse();
        assertThat(sut.coefficient(A)).isEqualTo(-3);
        assertThat(sut.coefficient(B)).isEqualTo(5);
        assertThat(sut.coefficient(X)).isZero();
    }

    @Test
    void shouldCopyAndPreserveEffectivePriority() {
        // Tests that copying a recipe preserves the effective priority setting independently.
        final LpPatternRecipe sut = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(A, 1).output(B, 1).build(),
            1
        );
        sut.setEffectivePriority(42);

        final LpPatternRecipe copy = sut.copy();
        copy.setEffectivePriority(99);

        assertThat(sut.effectivePriority()).isEqualTo(42);
        assertThat(copy.effectivePriority()).isEqualTo(99);
    }

    @Test
    void descriptionShouldRenderEmptySets() {
        // Tests that the description method correctly renders recipes with empty input or output sets.
        final Pattern pattern = new Pattern(
            UUID.randomUUID(),
            PatternLayout.internal(
                List.of(new Ingredient(1, List.of(A))),
                List.of(new ResourceAmount(B, 1)),
                List.of()
            )
        );
        final LpPatternRecipe sut = new LpPatternRecipe(pattern, new LpResourceSet(), new LpResourceSet(), 0, null);

        assertThat(sut.description()).isEqualTo("<empty> -> <empty>");
        assertThat(sut.toString()).isEqualTo(sut.description());
    }

    @Test
    void equalsAndHashCodeShouldUseUniqueId() {
        // Tests that equality and hashing are based on unique recipe IDs, not the resource details.
        final UUID id = UUID.randomUUID();
        final Pattern one = new Pattern(
            id,
            PatternLayout.internal(
                List.of(new Ingredient(1, List.of(A))),
                List.of(new ResourceAmount(B, 1)),
                List.of()
            )
        );
        final Pattern two = new Pattern(
            id,
            PatternLayout.internal(
                List.of(new Ingredient(2, List.of(B))),
                List.of(new ResourceAmount(C, 1)),
                List.of()
            )
        );

        final LpPatternRecipe left = new LpPatternRecipe(one, new LpResourceSet(), new LpResourceSet(), 1, null);
        final LpPatternRecipe right = new LpPatternRecipe(two, new LpResourceSet(), new LpResourceSet(), 2, 3);

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(id.hashCode());
        assertThat(right.hashCode()).isEqualTo(id.hashCode());
        assertThat(left).isNotEqualTo(new Object());
        assertThat(left.pattern()).isEqualTo(one);
        assertThat(left.uniqueId()).isEqualTo(id);
    }

    @Test
    void descriptionShouldRenderBothSidesWhenNonEmpty() {
        // Tests that the description method renders both input and output sides with correct amounts and byproducts.
        final LpPatternRecipe sut = LpPatternRecipe.fromPattern(
            PatternBuilder.pattern()
                .ingredient(A, 2)
                .output(B, 1)
                .byproduct(C, 4)
                .build(),
            0
        );

        final String description = sut.description();

        assertThat(description).contains("A x2");
        assertThat(description).contains("B x1");
        assertThat(description).contains("C x4");
        assertThat(description).contains(" -> ");
        assertThat(description).isEqualTo(sut.toString());
    }
}

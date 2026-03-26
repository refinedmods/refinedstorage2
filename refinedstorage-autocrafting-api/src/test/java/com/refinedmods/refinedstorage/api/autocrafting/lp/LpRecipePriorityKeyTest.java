package com.refinedmods.refinedstorage.api.autocrafting.lp;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpRecipePriorityKeyTest {
    @Test
    void appendShouldValidateInput() {
        final LpRecipePriorityKey sut = new LpRecipePriorityKey();

        assertThatThrownBy(() -> sut.appendRecipePriority(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("recipe cannot be null");
    }

    @Test
    void compareToShouldPreferHigherPriorityAtFirstDifference() {
        final LpRecipePriorityKey high = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(10));
        final LpRecipePriorityKey low = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(1));

        assertThat(high.compareTo(low)).isNegative();
        assertThat(low.compareTo(high)).isPositive();
    }

    @Test
    void compareToShouldUseLengthWhenPrefixesMatch() {
        final LpRecipePriorityKey shorter = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(5));
        final LpRecipePriorityKey longer = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(5))
            .appendRecipePriority(recipe(2));

        assertThat(shorter.compareTo(longer)).isNegative();
        assertThat(longer.compareTo(shorter)).isPositive();
    }

    @Test
    void equalsAndHashCodeShouldUsePriorityValues() {
        final LpRecipePriorityKey left = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(4))
            .appendRecipePriority(recipe(3));
        final LpRecipePriorityKey right = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(4))
            .appendRecipePriority(recipe(3));
        final LpRecipePriorityKey different = new LpRecipePriorityKey()
            .appendRecipePriority(recipe(4));

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
        assertThat(left.hashCode()).isNotZero();
        assertThat(left).isNotEqualTo(different);
        assertThat(left).isNotEqualTo("key");
    }

    private static LpPatternRecipe recipe(final int priority) {
        return LpPatternRecipe.fromPattern(pattern().ingredient(A, 1).output(B, 1).build(), priority);
    }
}

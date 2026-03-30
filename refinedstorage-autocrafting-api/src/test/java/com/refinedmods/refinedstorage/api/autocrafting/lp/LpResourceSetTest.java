package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LpResourceSetTest {
    @Test
    void constructorsAndFactoriesShouldHandleNullAndCopy() {
        // Tests that constructors and factory methods validate null inputs and perform defensive copying.
        assertThatThrownBy(() -> new LpResourceSet(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("amounts cannot be null");
        assertThatThrownBy(() -> LpResourceSet.copyOf(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("other cannot be null");

        final Map<com.refinedmods.refinedstorage.api.resource.ResourceKey, Long> source = new LinkedHashMap<>();
        source.put(A, 3L);
        final LpResourceSet copied = new LpResourceSet(source);
        source.put(A, 9L);

        assertThat(copied.getAmount(A)).isEqualTo(3);
    }

    @Test
    void fromResourceAmountsShouldAggregateValues() {
        // Tests that the factory method correctly aggregates resource amounts from a list.
        assertThatThrownBy(() -> LpResourceSet.fromResourceAmounts(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("resourceAmounts cannot be null");

        final LpResourceSet sut = LpResourceSet.fromResourceAmounts(List.of(
            new ResourceAmount(A, 2),
            new ResourceAmount(A, 3),
            new ResourceAmount(B, 1)
        ));

        assertThat(sut.getAmount(A)).isEqualTo(5);
        assertThat(sut.getAmount(B)).isEqualTo(1);
        assertThat(sut.totalAmount()).isEqualTo(6);
    }

    @Test
    void setAddSubtractAndRemoveShouldWork() {
        // Tests that set, add, and subtract operations modify resource amounts correctly and remove zero entries.
        final LpResourceSet sut = new LpResourceSet();

        sut.setAmount(A, 5);
        sut.addAmount(A, 2);
        sut.subtractAmount(A, 7);

        assertThat(sut.getAmount(A)).isZero();
        assertThat(sut.resourceKeys()).doesNotContain(A);

        sut.addAmount(B, 0);
        assertThat(sut.getAmount(B)).isZero();
    }

    @Test
    void addThenSubtractToZeroShouldResultInEmptySet() {
        // Tests that adding and subtracting the same amount fully removes the resource from the set.
        final LpResourceSet sut = new LpResourceSet();

        sut.addAmount(A, 3);
        sut.subtractAmount(A, 3);

        
        assertThat(sut.totalAmount()).isZero();
        assertThat(sut.getAmount(A)).isZero();
        assertThat(sut.resourceKeys()).isEmpty();
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void addThenAddNegativeToZeroShouldResultInEmptySet() {
        // Tests that adding a matching negative amount fully removes the resource from the set.
        final LpResourceSet sut = new LpResourceSet();

        sut.addAmount(A, 3);
        sut.addAmount(A, -3);

        assertThat(sut.totalAmount()).isZero();
        assertThat(sut.getAmount(A)).isZero();
        assertThat(sut.resourceKeys()).isEmpty();
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void shouldAllowAddToMakeAmountNegative() {
        // Tests that addAmount can lower a resource below zero.
        final LpResourceSet sut = new LpResourceSet();

        sut.addAmount(A, 2);
        sut.addAmount(A, -3);

        assertThat(sut.getAmount(A)).isEqualTo(-1);
        assertThat(sut.totalAmount()).isEqualTo(-1);
        assertThat(sut.isEmpty()).isFalse();
    }

    @Test
    void shouldAllowSubtractToMakeAmountNegative() {
        // Tests that subtractAmount can lower a resource below zero.
        final LpResourceSet sut = new LpResourceSet();

        sut.addAmount(A, 2);
        sut.subtractAmount(A, 3);

        assertThat(sut.getAmount(A)).isEqualTo(-1);
        assertThat(sut.totalAmount()).isEqualTo(-1);
        assertThat(sut.isEmpty()).isFalse();
    }

    @Test
    void shouldAllowSetAmountToNegativeValue() {
        // Tests that setAmount allows directly setting a negative amount.
        final LpResourceSet sut = new LpResourceSet();

        sut.setAmount(A, -3);

        assertThat(sut.getAmount(A)).isEqualTo(-3);
        assertThat(sut.totalAmount()).isEqualTo(-3);
        assertThat(sut.resourceKeys()).containsExactly(A);
        assertThat(sut.isEmpty()).isFalse();
    }

    @Test
    void setAmountZeroShouldKeepOrMakeSetEmpty() {
        // Tests that setAmount with zero removes entries and leaves the set empty when all values are zero.
        final LpResourceSet sut = new LpResourceSet();

        sut.setAmount(A, 0);
        assertThat(sut.isEmpty()).isTrue();

        sut.setAmount(A, 3);
        sut.setAmount(A, 0);

        assertThat(sut.getAmount(A)).isZero();
        assertThat(sut.totalAmount()).isZero();
        assertThat(sut.resourceKeys()).isEmpty();
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void addAllShouldMergeAndValidateInput() {
        // Tests that addAll merges multiple resource sets and validates null inputs.
        final LpResourceSet sut = new LpResourceSet();
        sut.setAmount(A, 1);

        final LpResourceSet other = new LpResourceSet();
        other.setAmount(A, 2);
        other.setAmount(B, 4);

        sut.addAll(other);

        assertThat(sut.getAmount(A)).isEqualTo(3);
        assertThat(sut.getAmount(B)).isEqualTo(4);

        assertThatThrownBy(() -> sut.addAll(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("other cannot be null");
    }

    @Test
    void shouldExposeUnmodifiableViews() {
        // Tests that views returned by the resource set are unmodifiable to prevent accidental mutations.
        final LpResourceSet sut = new LpResourceSet();
        sut.setAmount(A, 1);

        assertThat(sut.asMap()).containsEntry(A, 1L);
        assertThat(sut.resourceKeys()).containsExactly(A);

        assertThatThrownBy(() -> sut.asMap().put(B, 2L)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> sut.resourceKeys().remove(A)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCopyAndReportState() {
        // Tests that copy creates independent copies, and state reporting methods work correctly.
        final LpResourceSet sut = new LpResourceSet();
        sut.setAmount(A, 2);
        sut.setAmount(C, 3);

        final LpResourceSet copy = sut.copy();
        copy.addAmount(A, 5);

        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.totalAmount()).isEqualTo(5);
        assertThat(sut.getAmount(A)).isEqualTo(2);
        assertThat(copy.getAmount(A)).isEqualTo(7);
        assertThat(LpResourceSet.empty().isEmpty()).isTrue();
        assertThat(sut.toString()).contains("A", "C");
    }
}

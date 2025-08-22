package com.refinedmods.refinedstorage.api.autocrafting;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatternRepositoryImplTest {
    private static final Pattern PATTERN_A = pattern()
        .ingredient(C, 1)
        .output(A, 1)
        .byproduct(X, 1)
        .build();
    private static final Pattern PATTERN_AB = pattern()
        .ingredient(C, 1)
        .output(A, 1)
        .output(B, 1)
        .build();
    private static final Pattern PATTERN_B = pattern()
        .ingredient(C, 1)
        .output(B, 1)
        .build();

    private PatternRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        sut = new PatternRepositoryImpl();
    }

    @Test
    void testDefaultState() {
        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldAddPattern() {
        // Act
        sut.add(PATTERN_A, 0);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_A
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_A
        );
        assertThat(sut.getByOutput(X)).isEmpty();
    }

    @Test
    void shouldAddMultiplePatterns() {
        // Act
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_B, 0);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            PATTERN_A,
            PATTERN_B
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_A
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_B
        );
        assertThat(sut.getByOutput(C)).isEmpty();
    }

    @Test
    void shouldAddMultiplePatternsAndSomeWithTheSameOutput() {
        // Act
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_AB, 1);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            PATTERN_A,
            PATTERN_AB
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_AB,
            PATTERN_A
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_AB
        );
        assertThat(sut.getByOutput(C)).isEmpty();
    }

    @Test
    void shouldUpdatePriorityOfPattern() {
        // Arrange
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_AB, 1);

        // Act
        sut.update(PATTERN_A, 2);

        // Assert
        assertThat(sut.getByOutput(A)).containsExactly(PATTERN_A, PATTERN_AB);
    }

    @Test
    void shouldNotUpdatePriorityOfPatternsIfThePatternHasNotBeenAddedYet() {
        // Act
        sut.update(PATTERN_A, 1);

        // Assert
        assertThat(sut.getByOutput(A)).isEmpty();
    }

    @Test
    void shouldRemovePattern() {
        // Arrange
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_B, 0);

        // Act
        sut.remove(PATTERN_A);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(B);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_B
        );
        assertThat(sut.getByOutput(A)).isEmpty();
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_B
        );
    }

    @Test
    void shouldRemoveMultiplePatterns() {
        // Arrange
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_B, 0);

        // Act
        sut.remove(PATTERN_A);
        sut.remove(PATTERN_B);

        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getByOutput(A)).isEmpty();
        assertThat(sut.getByOutput(B)).isEmpty();
    }

    @Test
    void shouldRemovePatternButNotRemoveOutputIfAnotherPatternStillHasThatOutput() {
        // Arrange
        sut.add(PATTERN_A, 0);
        sut.add(PATTERN_AB, 0);

        // Act
        sut.remove(PATTERN_A);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_AB
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_AB
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            PATTERN_AB
        );
    }

    @Test
    void shouldRemovePatternThatWasNeverAddedInTheFirstPlace() {
        // Act
        sut.remove(PATTERN_A);

        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getByOutput(A)).isEmpty();
    }

    @Test
    void shouldNotSupportPatternsOrOutputsDirectly() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getOutputs().add(A);
        final ThrowableAssert.ThrowingCallable action2 = () -> sut.getAll().add(PATTERN_A);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(action2).isInstanceOf(UnsupportedOperationException.class);
    }
}

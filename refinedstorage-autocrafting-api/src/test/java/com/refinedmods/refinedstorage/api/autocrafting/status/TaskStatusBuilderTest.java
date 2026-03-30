package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;

import java.util.function.LongConsumer;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskStatusBuilderTest {
    @Test
    void shouldValidateStoredIsLargerThanZero() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act & assert
        assertRejectsNonPositive(value -> sut.stored(B, value));
    }

    @Test
    void shouldValidateExtractingIsLargerThanZero() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act & assert
        assertRejectsNonPositive(value -> sut.extracting(B, value));
    }

    @Test
    void shouldValidateProcessingIsLargerThanZero() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act & assert
        assertRejectsNonPositive(value -> sut.processing(B, value, null));
    }

    @Test
    void shouldValidateScheduledIsLargerThanZero() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act & assert
        assertRejectsNonPositive(value -> sut.scheduled(B, value));
    }

    @Test
    void shouldValidateCraftingIsLargerThanZero() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act & assert
        assertRejectsNonPositive(value -> sut.crafting(B, value));
    }

    @Test
    void shouldAcceptPositiveAmountsAndMapThemToStatus() {
        // Arrange
        final TaskStatusBuilder sut = createSut();

        // Act
        sut.stored(B, 1);
        sut.extracting(B, 1);
        sut.processing(B, 1, null);
        sut.scheduled(B, 1);
        sut.crafting(B, 1);
        final TaskStatus status = sut.build(0.0);

        // Assert
        assertThat(status.items()).singleElement().satisfies(item -> {
            assertThat(item.resource()).isEqualTo(B);
            assertThat(item.stored()).isEqualTo(1);
            assertThat(item.extracting()).isEqualTo(1);
            assertThat(item.processing()).isEqualTo(1);
            assertThat(item.scheduled()).isEqualTo(1);
            assertThat(item.crafting()).isEqualTo(1);
        });
    }

    private static void assertRejectsNonPositive(final LongConsumer action) {
        assertThatThrownBy(() -> action.accept(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> action.accept(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static TaskStatusBuilder createSut() {
        return new TaskStatusBuilder(TaskId.create(), TaskState.RUNNING, A, 1, 0);
    }
}
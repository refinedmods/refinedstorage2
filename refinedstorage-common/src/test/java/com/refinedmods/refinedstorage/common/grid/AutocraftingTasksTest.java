package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkDetails;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutocraftingTasksTest {
    private static final ResourceKey A = new TestResource("A");
    private static final ResourceKey B = new TestResource("B");
    private static final ResourceKey C = new TestResource("C");

    private static final ExternalPatternSinkDetails DETAILS_1 = new TestDetails("1");
    private static final ExternalPatternSinkDetails DETAILS_2 = new TestDetails("2");

    private AutocraftingTasks sut;

    @BeforeEach
    void setUp() {
        sut = new AutocraftingTasks();
    }

    @Test
    void shouldReturnEmptyForUnknownResource() {
        // Assert
        assertThat(sut.getStatuses(A)).isEmpty();
        assertThat(sut.getMergedItems(A)).isEmpty();
    }

    @Test
    void shouldReturnItemsUnchangedForSingleStatus() {
        // Arrange
        final TaskStatus.Item item1 = item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5);
        final TaskStatus.Item item2 = item(B, TaskStatus.ItemType.REJECTED, null, 10, 20, 30, 40, 50);
        final TaskStatus status = status(TaskId.create(), A, List.of(item1, item2));

        // Act
        sut.addOrUpdateStatus(status);

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(status);
        assertThat(sut.getMergedItems(A)).containsExactly(item1, item2);
    }

    @Test
    void shouldMergeItemsAcrossStatusesForSameResource() {
        // Arrange
        final TaskStatus status1 = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        ));
        final TaskStatus status2 = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 10, 20, 30, 40, 50)
        ));

        // Act
        sut.addOrUpdateStatus(status1);
        sut.addOrUpdateStatus(status2);

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(status1, status2);
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 11, 22, 33, 44, 55)
        );
    }

    @Test
    void shouldMergeItemsWithinSameStatus() {
        // Arrange
        final TaskStatus status = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5),
            item(A, TaskStatus.ItemType.NORMAL, null, 10, 20, 30, 40, 50)
        ));

        // Act
        sut.addOrUpdateStatus(status);

        // Assert
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 11, 22, 33, 44, 55)
        );
    }

    @Test
    void shouldKeepItemsSeparateWhenTypeDiffers() {
        // Arrange
        final TaskStatus status = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.REJECTED, null, 2, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NONE_FOUND, null, 3, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.LOCKED, null, 4, 0, 0, 0, 0)
        ));

        // Act
        sut.addOrUpdateStatus(status);

        // Assert
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.REJECTED, null, 2, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NONE_FOUND, null, 3, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.LOCKED, null, 4, 0, 0, 0, 0)
        );
    }

    @Test
    void shouldKeepItemsSeparateWhenDetailsDiffers() {
        // Arrange
        final TaskStatus status = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_1, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_2, 2, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, null, 3, 0, 0, 0, 0)
        ));

        // Act
        sut.addOrUpdateStatus(status);

        // Assert
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_1, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_2, 2, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, null, 3, 0, 0, 0, 0)
        );
    }

    @Test
    void shouldMergeItemsWithSameDetails() {
        // Arrange
        final TaskStatus status1 = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_1, 1, 2, 3, 4, 5)
        ));
        final TaskStatus status2 = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_1, 10, 20, 30, 40, 50)
        ));

        // Act
        sut.addOrUpdateStatus(status1);
        sut.addOrUpdateStatus(status2);

        // Assert
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, DETAILS_1, 11, 22, 33, 44, 55)
        );
    }

    @Test
    void shouldKeepResourcesIsolated() {
        // Arrange
        final TaskStatus statusA = status(TaskId.create(), A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0)
        ));
        final TaskStatus statusB = status(TaskId.create(), B, List.of(
            item(B, TaskStatus.ItemType.NORMAL, null, 2, 0, 0, 0, 0)
        ));

        // Act
        sut.addOrUpdateStatus(statusA);
        sut.addOrUpdateStatus(statusB);

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(statusA);
        assertThat(sut.getStatuses(B)).containsExactly(statusB);
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0)
        );
        assertThat(sut.getMergedItems(B)).containsExactly(
            item(B, TaskStatus.ItemType.NORMAL, null, 2, 0, 0, 0, 0)
        );
    }

    @Test
    void shouldReplaceStatusOnUpdateWithSameTaskId() {
        // Arrange
        final TaskId id = TaskId.create();
        final TaskStatus initial = status(id, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        ));
        final TaskStatus updated = status(id, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 100, 200, 300, 400, 500)
        ));

        // Act
        sut.addOrUpdateStatus(initial);
        sut.addOrUpdateStatus(updated);

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(updated);
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 100, 200, 300, 400, 500)
        );
    }

    @Test
    void shouldRemoveSingleStatusAndKeepRemaining() {
        // Arrange
        final TaskId id1 = TaskId.create();
        final TaskId id2 = TaskId.create();
        final TaskStatus status1 = status(id1, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        ));
        final TaskStatus status2 = status(id2, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 10, 20, 30, 40, 50)
        ));
        sut.addOrUpdateStatus(status1);
        sut.addOrUpdateStatus(status2);

        // Act
        sut.removeStatus(id1);

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(status2);
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 10, 20, 30, 40, 50)
        );
    }

    @Test
    void shouldClearResourceWhenLastStatusRemoved() {
        // Arrange
        final TaskId id = TaskId.create();
        sut.addOrUpdateStatus(status(id, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        )));

        // Act
        sut.removeStatus(id);

        // Assert
        assertThat(sut.getStatuses(A)).isEmpty();
        assertThat(sut.getMergedItems(A)).isEmpty();
    }

    @Test
    void shouldDoNothingWhenRemovingUnknownTaskId() {
        // Arrange
        final TaskId id = TaskId.create();
        final TaskStatus status = status(id, A, List.of(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        ));
        sut.addOrUpdateStatus(status);

        // Act
        sut.removeStatus(TaskId.create());

        // Assert
        assertThat(sut.getStatuses(A)).containsExactly(status);
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(A, TaskStatus.ItemType.NORMAL, null, 1, 2, 3, 4, 5)
        );
    }

    @Test
    void shouldPreserveInsertionOrderInMergedItems() {
        // Arrange
        final TaskStatus status1 = status(TaskId.create(), A, List.of(
            item(C, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, null, 2, 0, 0, 0, 0)
        ));
        final TaskStatus status2 = status(TaskId.create(), A, List.of(
            item(B, TaskStatus.ItemType.NORMAL, null, 3, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, null, 4, 0, 0, 0, 0)
        ));

        // Act
        sut.addOrUpdateStatus(status1);
        sut.addOrUpdateStatus(status2);

        // Assert
        assertThat(sut.getMergedItems(A)).containsExactly(
            item(C, TaskStatus.ItemType.NORMAL, null, 1, 0, 0, 0, 0),
            item(A, TaskStatus.ItemType.NORMAL, null, 6, 0, 0, 0, 0),
            item(B, TaskStatus.ItemType.NORMAL, null, 3, 0, 0, 0, 0)
        );
    }

    private static TaskStatus.Item item(final ResourceKey resource,
                                        final TaskStatus.ItemType type,
                                        @Nullable final ExternalPatternSinkDetails details,
                                        final long stored,
                                        final long extracting,
                                        final long processing,
                                        final long scheduled,
                                        final long crafting) {
        return new TaskStatus.Item(resource, type, details, stored, extracting, processing, scheduled, crafting);
    }

    private static TaskStatus status(final TaskId id,
                                     final ResourceKey resource,
                                     final List<TaskStatus.Item> items) {
        return new TaskStatus(
            new TaskStatus.TaskInfo(id, resource, 1L, 0L),
            TaskState.RUNNING,
            0.0,
            items
        );
    }

    private record TestResource(String name) implements ResourceKey {
    }

    private record TestDetails(String name) implements ExternalPatternSinkDetails {
    }
}

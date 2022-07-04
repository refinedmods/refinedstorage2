package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GridViewImplTest {
    private GridView<String> view;

    @BeforeEach
    void setUp() {
        view = new GridViewImpl<>(FakeGridResource::new, new ResourceListImpl<>());
        view.setSortingType(GridSortingType.QUANTITY);
    }

    @RepeatedTest(100)
    void shouldPreserveOrderWhenSortingAndTwoResourcesHaveTheSameQuantity() {
        // Arrange
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        // Act & assert
        view.onChange("A", 10, null);
        view.onChange("A", 5, null);
        view.onChange("B", 15, null);
        view.onChange("C", 2, null);

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 15),
            new FakeGridResource("A", 15),
            new FakeGridResource("C", 2)
        );

        view.onChange("A", -15, null);
        view.onChange("A", 15, null);

        view.onChange("B", -15, null);
        view.onChange("B", 15, null);

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 15),
            new FakeGridResource("A", 15),
            new FakeGridResource("C", 2)
        );
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void testSortingAscending(final GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, new TrackedResource("Raoul", 3));
        view.loadResource("B", 1, new TrackedResource("VdB", 2));
        view.loadResource("C", 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("B", 1),
                new FakeGridResource("C", 2),
                new FakeGridResource("A", 15)
            );
            case NAME -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("A", 15),
                new FakeGridResource("B", 1),
                new FakeGridResource("C", 2)
            );
            case ID -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("A", 15),
                new FakeGridResource("B", 1),
                new FakeGridResource("C", 2)
            );
            case LAST_MODIFIED ->
                assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridResource("C", 2),
                    new FakeGridResource("B", 1),
                    new FakeGridResource("A", 15)
                );
            default -> fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void testSortingDescending(final GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, new TrackedResource("Raoul", 3));
        view.loadResource("B", 1, new TrackedResource("VDB", 2));
        view.loadResource("C", 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("A", 15),
                new FakeGridResource("C", 2),
                new FakeGridResource("B", 1)
            );
            case NAME -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("C", 2),
                new FakeGridResource("B", 1),
                new FakeGridResource("A", 15)
            );
            case ID -> assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("C", 2),
                new FakeGridResource("B", 1),
                new FakeGridResource("A", 15)
            );
            case LAST_MODIFIED ->
                assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridResource("A", 15),
                    new FakeGridResource("B", 1),
                    new FakeGridResource("C", 2)
                );
            default -> fail();
        }
    }

    @Test
    void shouldLoadResourcesAndRetrieveTrackedResourcesProperly() {
        // Act
        view.loadResource("A", 1, new TrackedResource("Raoul", 1));
        view.loadResource("A", 1, new TrackedResource("RaoulA", 2));

        view.loadResource("B", 1, new TrackedResource("VDB", 3));
        view.loadResource("B", 1, null);

        view.loadResource("D", 1, null);

        // Assert
        final Optional<TrackedResource> a = view.getTrackedResource("A");
        final Optional<TrackedResource> b = view.getTrackedResource("B");
        final Optional<TrackedResource> d = view.getTrackedResource("D");

        assertThat(a).get().usingRecursiveComparison().isEqualTo(new TrackedResource("RaoulA", 2));
        assertThat(b).isEmpty();
        assertThat(d).isEmpty();
    }

    @Test
    void shouldInsertNewResource() {
        // Arrange
        view.loadResource("B", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("A", 12, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 12),
            new FakeGridResource("B", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void shouldNotInsertNewResourceWhenFilteringProhibitsIt() {
        // Arrange
        view.loadResource("B", 15, null);
        view.loadResource("D", 10, null);
        view.setFilterAndSort(resource -> !resource.getResourceAmount().getResource().equals("A"));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("A", 12, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("B", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void shouldUpdateExistingResource() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", 5, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("B", 11),
            new FakeGridResource("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void shouldNotUpdateExistingResourceWhenFilteringProhibitsIt() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.setFilterAndSort(resource -> !resource.getResourceAmount().getResource().equals("B"));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", 5, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void shouldNotReorderExistingResourceWhenPreventingSorting() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 6),
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );

        view.setPreventSorting(true);

        view.onChange("B", 5, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 11),
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("B", 11),
            new FakeGridResource("A", 15)
        );
    }

    @Test
    void shouldUpdateTrackedResourceAfterReceivingChange() {
        // Act
        view.onChange("A", 1, new TrackedResource("Raoul", 1));
        view.onChange("A", 1, new TrackedResource("RaoulA", 2));

        view.onChange("B", 1, new TrackedResource("VDB", 3));
        view.onChange("B", 1, null);

        view.onChange("D", 1, null);

        // Assert
        final Optional<TrackedResource> a = view.getTrackedResource("A");
        final Optional<TrackedResource> b = view.getTrackedResource("B");
        final Optional<TrackedResource> c = view.getTrackedResource("D");

        assertThat(a).get().usingRecursiveComparison().isEqualTo(new TrackedResource("RaoulA", 2));
        assertThat(b).isEmpty();
        assertThat(c).isEmpty();
    }

    @Test
    void shouldUpdateExistingResourceWhenPerformingPartialRemoval() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -7, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("B", 13),
            new FakeGridResource("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void shouldNotUpdateExistingResourceWhenPerformingPartialRemovalAndFilteringProhibitsIt() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.setFilterAndSort(resource -> !resource.getResourceAmount().getResource().equals("B"));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -7, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void shouldNotReorderExistingResourceWhenPerformingPartialRemovalAndPreventingSorting() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 20)
        );

        view.setPreventSorting(true);

        view.onChange("B", -7, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 13)
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("B", 13),
            new FakeGridResource("A", 15)
        );
    }

    @Test
    void shouldRemoveExistingResourceCompletely() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -20, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void shouldNotReorderWhenRemovingExistingResourceCompletelyAndPreventingSorting() {
        // Arrange
        view.loadResource("A", 15, null);
        view.loadResource("B", 20, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 20)
        );

        view.setPreventSorting(true);
        view.onChange("B", -20, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 20).zeroed()
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );
    }

    @Test
    void shouldReuseExistingResourceWhenPreventingSortingAndRemovingExistingResourceCompletelyAndThenReinserting() {
        // Arrange
        view.loadResource("A", 15, null);
        view.loadResource("B", 20, null);
        view.loadResource("D", 10, null);
        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 20)
        );

        // Delete the item
        view.setPreventSorting(true);
        view.onChange("B", -20, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 20).zeroed()
        );

        // Re-insert the item
        view.onChange("B", 5, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 5)
        );

        // Re-insert the item again
        view.onChange("B", 3, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15),
            new FakeGridResource("B", 8)
        );
    }
}

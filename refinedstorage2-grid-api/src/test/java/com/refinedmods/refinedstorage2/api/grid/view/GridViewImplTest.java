package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GridViewImplTest {
    private GridViewBuilder viewBuilder;

    @BeforeEach
    void setUp() {
        viewBuilder = new GridViewBuilderImpl(resourceAmount -> Optional.of(new FakeGridResource(resourceAmount)));
    }

    @Test
    void shouldAddResourcesWithSameNameButDifferentIdentity() {
        // Ensure that we do not get in trouble when adding 2 resources with the same name, but a different identity.
        // This test avoids the bug where the view insertion fails, because the resource is already "contained"
        // in the view, but actually isn't because it has a different identity.

        // Arrange
        final GridViewBuilder builder = new GridViewBuilderImpl(resourceAmount -> Optional.of(
            new GridResourceWithMetadata(resourceAmount)
        ));
        final GridView view = builder.build();

        // Act
        view.onChange(new ResourceWithMetadata("A", 1), 1, null);
        view.onChange(new ResourceWithMetadata("A", 2), 1, null);

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceWithMetadata(new ResourceAmount<>(
                new ResourceWithMetadata("A", 1), 1
            )),
            new GridResourceWithMetadata(new ResourceAmount<>(
                new ResourceWithMetadata("A", 2), 1
            ))
        );
    }

    @Test
    void shouldPreserveOrderWhenSortingAndTwoResourcesHaveTheSameQuantity() {
        // Arrange
        final GridView view = viewBuilder.build();

        view.setSortingDirection(GridSortingDirection.DESCENDING);
        view.setSortingType(GridSortingType.QUANTITY);

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
        final GridView view = viewBuilder
            .withResource("A", 10, null)
            .withResource("A", 5, new TrackedResource("Raoul", 3))
            .withResource("B", 1, new TrackedResource("VdB", 2))
            .withResource("C", 2, null)
            .build();

        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

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
        final GridView view = viewBuilder
            .withResource("A", 10, null)
            .withResource("A", 5, new TrackedResource("Raoul", 3))
            .withResource("B", 1, new TrackedResource("VDB", 2))
            .withResource("C", 2, null)
            .build();

        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

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
        // Arrange
        final GridView view = viewBuilder
            .withResource("A", 1, new TrackedResource("Raoul", 1))
            .withResource("A", 1, new TrackedResource("RaoulA", 2))
            .withResource("B", 1, new TrackedResource("VDB", 3))
            .withResource("B", 1, null)
            .withResource("D", 1, null)
            .build();

        // Act
        final Optional<TrackedResource> a = view.getTrackedResource("A");
        final Optional<TrackedResource> b = view.getTrackedResource("B");
        final Optional<TrackedResource> d = view.getTrackedResource("D");

        // Assert
        assertThat(a).get().usingRecursiveComparison().isEqualTo(new TrackedResource("RaoulA", 2));
        assertThat(b).isEmpty();
        assertThat(d).isEmpty();
    }

    @Test
    void shouldInsertNewResource() {
        // Arrange
        final GridView view = viewBuilder
            .withResource("B", 15, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("B", 15, null)
            .withResource("D", 10, null)
            .build();

        view.setFilterAndSort(resource -> !resource.getName().equals("A"));

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
    void shouldCallListenerWhenSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource("B", 6, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.sort();

        // Assert
        verify(listener, times(1)).run();
        verifyNoMoreInteractions(listener);
    }

    @Test
    void shouldUpdateExistingResource() {
        // Arrange
        final GridView view = viewBuilder
            .withResource("B", 6, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("B", 6, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

        view.setFilterAndSort(resource -> !resource.getName().equals("B"));

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
        final GridView view = viewBuilder
            .withResource("B", 6, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 6),
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );

        final boolean changed = view.setPreventSorting(true);
        assertThat(changed).isTrue();
        final boolean changed2 = view.setPreventSorting(true);
        assertThat(changed2).isFalse();

        view.onChange("B", 5, null);
        verify(listener, never()).run();

        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new FakeGridResource("B", 11),
            new FakeGridResource("D", 10),
            new FakeGridResource("A", 15)
        );

        final boolean changed3 = view.setPreventSorting(false);
        assertThat(changed3).isTrue();
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
        final GridView view = viewBuilder.build();

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
        final GridView view = viewBuilder
            .withResource("B", 20, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("B", 20, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

        view.setFilterAndSort(resource -> !resource.getName().equals("B"));

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
        final GridView view = viewBuilder
            .withResource("B", 20, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("B", 20, null)
            .withResource("A", 15, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("A", 15, null)
            .withResource("B", 20, null)
            .withResource("D", 10, null)
            .build();

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
        final GridView view = viewBuilder
            .withResource("A", 15, null)
            .withResource("B", 20, null)
            .withResource("D", 10, null)
            .build();

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

    private record ResourceWithMetadata(String name, int metadata) {
    }

    private static class GridResourceWithMetadata extends AbstractGridResource {
        GridResourceWithMetadata(final ResourceAmount<?> resourceAmount) {
            super(resourceAmount, ((ResourceWithMetadata) resourceAmount.getResource()).name(), Map.of());
        }

        @Override
        public int getId() {
            return 0;
        }
    }
}

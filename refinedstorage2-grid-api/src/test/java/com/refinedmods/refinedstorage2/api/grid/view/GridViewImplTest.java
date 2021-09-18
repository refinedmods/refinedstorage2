package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.test.Rs2Test;

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

@Rs2Test
class GridViewImplTest {
    private GridView<String> view;

    @BeforeEach
    void setUp() {
        view = new GridViewImpl<>(FakeGridStack::new, new StackListImpl<>());

        view.setSortingType(GridSortingType.QUANTITY);
    }

    @Test
    void Test_sorting_ascending_with_identity_sort() {
        // Arrange
        view.setSortingType(null);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, null);
        view.loadResource("C", 1, null);
        view.loadResource("B", 2, null);

        // Act
        view.sort();

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 2),
                new FakeGridStack("C", 1)
        );
    }

    @Test
    void Test_sorting_descending_with_identity_sorting() {
        // Arrange
        view.setSortingType(null);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, null);
        view.loadResource("B", 1, null);
        view.loadResource("C", 2, null);

        // Act
        view.sort();

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("C", 2),
                new FakeGridStack("B", 1),
                new FakeGridStack("A", 15)
        );
    }

    @RepeatedTest(100)
    void Test_sorting_when_both_stacks_match_should_preserve_order() {
        // Arrange
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        // Act & assert
        view.onChange("A", 10, null);
        view.onChange("A", 5, null);
        view.onChange("B", 15, null);
        view.onChange("C", 2, null);

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("B", 15),
                new FakeGridStack("A", 15),
                new FakeGridStack("C", 2)
        );

        view.onChange("A", -15, null);
        view.onChange("A", 15, null);

        view.onChange("B", -15, null);
        view.onChange("B", 15, null);

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("B", 15),
                new FakeGridStack("A", 15),
                new FakeGridStack("C", 2)
        );
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void Test_sorting_ascending(GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, new StorageTracker.Entry(3, "Raoul"));
        view.loadResource("B", 1, new StorageTracker.Entry(2, "VdB"));
        view.loadResource("C", 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("B", 1),
                    new FakeGridStack("C", 2),
                    new FakeGridStack("A", 15)
            );
            case NAME -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("A", 15),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("C", 2)
            );
            case ID -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("A", 15),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("C", 2)
            );
            case LAST_MODIFIED -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("C", 2),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("A", 15)
            );
            default -> fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void Test_sorting_descending(GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadResource("A", 10, null);
        view.loadResource("A", 5, new StorageTracker.Entry(3, "Raoul"));
        view.loadResource("B", 1, new StorageTracker.Entry(2, "VDB"));
        view.loadResource("C", 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("A", 15),
                    new FakeGridStack("C", 2),
                    new FakeGridStack("B", 1)
            );
            case NAME -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("C", 2),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("A", 15)
            );
            case ID -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("C", 2),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("A", 15)
            );
            case LAST_MODIFIED -> assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new FakeGridStack("A", 15),
                    new FakeGridStack("B", 1),
                    new FakeGridStack("C", 2)
            );
            default -> fail();
        }
    }

    @Test
    void Test_loading_stack_with_storage_tracker_entry() {
        // Act
        view.loadResource("A", 1, new StorageTracker.Entry(1, "Raoul"));
        view.loadResource("A", 1, new StorageTracker.Entry(2, "RaoulA"));

        view.loadResource("B", 1, new StorageTracker.Entry(3, "VDB"));
        view.loadResource("B", 1, null);

        view.loadResource("D", 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry("A");
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry("B");
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry("D");

        assertThat(dirt).isPresent();
        assertThat(dirt.get().name()).isEqualTo("RaoulA");
        assertThat(dirt.get().time()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_addition_for_new_stack() {
        // Arrange
        view.loadResource("B", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("A", 12, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 12),
                new FakeGridStack("B", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_filtering() {
        // Arrange
        view.loadResource("B", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> !stack.getResourceAmount().getResource().equals("A"));

        // Act
        view.onChange("A", 12, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("B", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_preventing_sort() {
        // Arrange
        view.loadResource("B", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setPreventSorting(true);

        // Act
        view.onChange("A", 12, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 12),
                new FakeGridStack("B", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", 5, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("B", 11),
                new FakeGridStack("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_filtering() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> !stack.getResourceAmount().getResource().equals("B"));

        // Act
        view.onChange("B", 5, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_but_hidden_stack_when_filtering() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.setFilter(stack -> !stack.getResourceAmount().getResource().equals("B"));
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", 5, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_preventing_sort() {
        // Arrange
        view.loadResource("B", 6, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("B", 6),
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );

        view.setPreventSorting(true);

        view.onChange("B", 5, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("B", 11),
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("B", 11),
                new FakeGridStack("A", 15)
        );
    }

    @Test
    void Test_sending_change_should_set_storage_tracker_entry() {
        // Act
        view.onChange("A", 1, new StorageTracker.Entry(1, "Raoul"));
        view.onChange("A", 1, new StorageTracker.Entry(2, "RaoulA"));

        view.onChange("B", 1, new StorageTracker.Entry(3, "VDB"));
        view.onChange("B", 1, null);

        view.onChange("D", 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry("A");
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry("B");
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry("D");

        assertThat(dirt).isPresent();
        assertThat(dirt.get().name()).isEqualTo("RaoulA");
        assertThat(dirt.get().time()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_removal() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -7, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("B", 13),
                new FakeGridStack("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_when_filtering() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> !stack.getResourceAmount().getResource().equals("B"));

        // Act
        view.onChange("B", -7, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_for_hidden_stack_when_filtering() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.setFilter(stack -> !stack.getResourceAmount().getResource().equals("B"));
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -7, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_removal_when_preventing_sort() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 20)
        );

        view.setPreventSorting(true);

        view.onChange("B", -7, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 13)
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("B", 13),
                new FakeGridStack("A", 15)
        );
    }

    @Test
    void Test_sending_complete_removal() {
        // Arrange
        view.loadResource("B", 20, null);
        view.loadResource("A", 15, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange("B", -20, null);

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_complete_removal_when_preventing_sort() {
        // Arrange
        view.loadResource("A", 15, null);
        view.loadResource("B", 20, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 20)
        );

        view.setPreventSorting(true);
        view.onChange("B", -20, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 20).zeroed()
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15)
        );
    }

    @Test
    void Test_sending_complete_removal_and_reinserting_stack_should_reuse_same_stack_when_preventing_sort() {
        // Arrange
        view.loadResource("A", 15, null);
        view.loadResource("B", 20, null);
        view.loadResource("D", 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 20)
        );

        // Delete the item
        view.setPreventSorting(true);
        view.onChange("B", -20, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 20).zeroed()
        );

        // Re-insert the item
        view.onChange("B", 5, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 5)
        );

        // Re-insert the item again
        view.onChange("B", 3, null);
        verify(listener, never()).run();

        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("D", 10),
                new FakeGridStack("A", 15),
                new FakeGridStack("B", 8)
        );
    }
}

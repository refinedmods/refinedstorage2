package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.grid.GridStackAssertions.assertOrderedItemGridStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Rs2Test
class GridViewImplTest {
    private GridView<Rs2ItemStack> view;

    @BeforeEach
    void setUp() {
        view = new GridViewImpl<>(new FakeGridStackFactory(), Rs2ItemStackIdentifier::new, StackListImpl.createItemStackList());
        view.setSortingType(GridSortingType.QUANTITY);
    }

    @Test
    void Test_sorting_ascending_with_identity_sort() {
        // Arrange
        view.setSortingType(null);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 10, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 5, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        assertOrderedItemGridStackListContents(
                view.getStacks(),
                new Rs2ItemStack(ItemStubs.BUCKET, 2),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.GLASS, 1)
        );
    }

    @Test
    void Test_sorting_descending_with_identity_sort() {
        // Arrange
        view.setSortingType(null);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 10, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 5, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        assertOrderedItemGridStackListContents(
                view.getStacks(),
                new Rs2ItemStack(ItemStubs.GLASS, 1),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.BUCKET, 2)
        );
    }

    @RepeatedTest(100)
    void Test_sorting_when_both_stacks_match_should_preserve_order() {
        // Arrange
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        // Act & assert
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 10, null);
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 5, null);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 15, null);
        view.onChange(new Rs2ItemStack(ItemStubs.BUCKET), 2, null);

        assertOrderedItemGridStackListContents(
                view.getStacks(),
                new Rs2ItemStack(ItemStubs.GLASS, 15),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.BUCKET, 2)
        );

        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), -15, null);
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 15, null);

        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -15, null);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 15, null);

        assertOrderedItemGridStackListContents(
                view.getStacks(),
                new Rs2ItemStack(ItemStubs.GLASS, 15),
                new Rs2ItemStack(ItemStubs.DIRT, 15),
                new Rs2ItemStack(ItemStubs.BUCKET, 2)
        );
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void Test_sorting_ascending(GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 10, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 5, new StorageTracker.Entry(3, "Raoul"));
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, new StorageTracker.Entry(2, "VdB"));
        view.loadStack(new Rs2ItemStack(ItemStubs.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2),
                        new Rs2ItemStack(ItemStubs.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2),
                        new Rs2ItemStack(ItemStubs.DIRT, 15),
                        new Rs2ItemStack(ItemStubs.GLASS, 1)
                );
                break;
            case ID:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.DIRT, 15),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2)
                );
                break;
            case LAST_MODIFIED:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.DIRT, 15)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSortingType.class)
    void Test_sorting_descending(GridSortingType sortingType) {
        // Arrange
        view.setSortingType(sortingType);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 10, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 5, new StorageTracker.Entry(3, "Raoul"));
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, new StorageTracker.Entry(2, "VDB"));
        view.loadStack(new Rs2ItemStack(ItemStubs.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.DIRT, 15),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2),
                        new Rs2ItemStack(ItemStubs.GLASS, 1)
                );
                break;
            case NAME:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.DIRT, 15),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2)
                );
                break;
            case ID:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.DIRT, 15)
                );
                break;
            case LAST_MODIFIED:
                assertOrderedItemGridStackListContents(
                        view.getStacks(),
                        new Rs2ItemStack(ItemStubs.DIRT, 15),
                        new Rs2ItemStack(ItemStubs.GLASS, 1),
                        new Rs2ItemStack(ItemStubs.BUCKET, 2)
                );
                break;
            default:
                fail();
        }
    }

    @Test
    void Test_loading_stack_with_storage_tracker_entry() {
        // Act
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 1, new StorageTracker.Entry(1, "Raoul"));
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 1, new StorageTracker.Entry(2, "RaoulA"));

        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, new StorageTracker.Entry(3, "VDB"));
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 1, null);

        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.DIRT));
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.GLASS));
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.SPONGE));

        assertThat(dirt).isPresent();
        assertThat(dirt.get().getName()).isEqualTo("RaoulA");
        assertThat(dirt.get().getTime()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_addition_for_new_stack() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 12), new Rs2ItemStack(ItemStubs.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_filtering() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> ((Rs2ItemStack) stack.getStack()).getItem() != ItemStubs.DIRT);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.GLASS, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setPreventSorting(true);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 12), new Rs2ItemStack(ItemStubs.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 6, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.GLASS, 11), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_filtering() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 6, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> ((Rs2ItemStack) stack.getStack()).getItem() != ItemStubs.GLASS);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_but_hidden_stack_when_filtering() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 6, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.setFilter(stack -> ((Rs2ItemStack) stack.getStack()).getItem() != ItemStubs.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 6, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 6), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));

        view.setPreventSorting(true);

        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 5, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 11), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.GLASS, 11), new Rs2ItemStack(ItemStubs.DIRT, 15));
    }

    @Test
    void Test_sending_change_should_set_storage_tracker_entry() {
        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 1, new StorageTracker.Entry(1, "Raoul"));
        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 1, new StorageTracker.Entry(2, "RaoulA"));

        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 1, new StorageTracker.Entry(3, "VDB"));
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 1, null);

        view.onChange(new Rs2ItemStack(ItemStubs.SPONGE), 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.DIRT));
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.GLASS));
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry(new Rs2ItemStack(ItemStubs.SPONGE));

        assertThat(dirt).isPresent();
        assertThat(dirt.get().getName()).isEqualTo("RaoulA");
        assertThat(dirt.get().getTime()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_removal() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.GLASS, 13), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_when_filtering() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> ((Rs2ItemStack) stack.getStack()).getItem() != ItemStubs.GLASS);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_for_hidden_stack_when_filtering() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.setFilter(stack -> ((Rs2ItemStack) stack.getStack()).getItem() != ItemStubs.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_removal_when_preventing_sort() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 20));

        view.setPreventSorting(true);

        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -7, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 13));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.GLASS, 13), new Rs2ItemStack(ItemStubs.DIRT, 15));
    }

    @Test
    void Test_sending_complete_removal() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -20, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_complete_removal_when_preventing_sort() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 20));

        view.setPreventSorting(true);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -20, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 20));

        assertThat(view.getStacks()).anyMatch(stack -> stack.getStack().getItem() == ItemStubs.GLASS && stack.isZeroed());

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15));
    }

    @Test
    void Test_sending_complete_removal_and_reinserting_stack_should_reuse_same_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new Rs2ItemStack(ItemStubs.DIRT), 15, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.GLASS), 20, null);
        view.loadStack(new Rs2ItemStack(ItemStubs.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 20));

        // Delete the item
        view.setPreventSorting(true);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), -20, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 20));
        assertThat(view.getStacks()).anyMatch(stack -> stack.getStack().getItem() == ItemStubs.GLASS && stack.isZeroed());

        // Re-insert the item
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 5, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 5));
        assertThat(view.getStacks()).noneMatch(GridStack::isZeroed);

        // Re-insert the item again
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 3, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.SPONGE, 10), new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.GLASS, 8));
        assertThat(view.getStacks()).noneMatch(GridStack::isZeroed);
    }
}

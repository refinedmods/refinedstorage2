package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.ItemStackIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemGridStackListContents;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertOrderedItemGridStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@RefinedStorage2Test
public class GridViewTest {
    private GridView<ItemStack> view;

    @BeforeEach
    void setUp() {
        view = new GridViewImpl<>(new FakeGridStackFactory(), ItemStackIdentifier::new, GridSorter.NAME.getComparator(), new ItemStackList());
        view.setSorter(GridSorter.QUANTITY.getComparator());
    }

    @Test
    void Test_sorting_ascending_with_identity_sort() {
        // Arrange
        view.setSorter(null);
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10, null);
        view.loadStack(new ItemStack(Items.DIRT), 5, null);
        view.loadStack(new ItemStack(Items.GLASS), 1, null);
        view.loadStack(new ItemStack(Items.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        assertOrderedItemGridStackListContents(
            view.getStacks(),
            new ItemStack(Items.BUCKET, 2),
            new ItemStack(Items.DIRT, 15),
            new ItemStack(Items.GLASS, 1)
        );
    }

    @Test
    void Test_sorting_descending_with_identity_sort() {
        // Arrange
        view.setSorter(null);
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10, null);
        view.loadStack(new ItemStack(Items.DIRT), 5, null);
        view.loadStack(new ItemStack(Items.GLASS), 1, null);
        view.loadStack(new ItemStack(Items.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        assertOrderedItemGridStackListContents(
            view.getStacks(),
            new ItemStack(Items.GLASS, 1),
            new ItemStack(Items.DIRT, 15),
            new ItemStack(Items.BUCKET, 2)
        );
    }

    @RepeatedTest(100)
    void Test_sorting_when_both_stacks_match_should_preserve_order() {
        // Arrange
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        // Act & assert
        view.onChange(new ItemStack(Items.DIRT), 10, null);
        view.onChange(new ItemStack(Items.DIRT), 5, null);
        view.onChange(new ItemStack(Items.GLASS), 15, null);
        view.onChange(new ItemStack(Items.BUCKET), 2, null);

        assertOrderedItemGridStackListContents(
            view.getStacks(),
            new ItemStack(Items.GLASS, 15),
            new ItemStack(Items.DIRT, 15),
            new ItemStack(Items.BUCKET, 2)
        );

        view.onChange(new ItemStack(Items.DIRT), -15, null);
        view.onChange(new ItemStack(Items.DIRT), 15, null);

        view.onChange(new ItemStack(Items.GLASS), -15, null);
        view.onChange(new ItemStack(Items.GLASS), 15, null);

        assertOrderedItemGridStackListContents(
            view.getStacks(),
            new ItemStack(Items.GLASS, 15),
            new ItemStack(Items.DIRT, 15),
            new ItemStack(Items.BUCKET, 2)
        );
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sorting_ascending(GridSorter sorter) {
        // Arrange
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10, null);
        view.loadStack(new ItemStack(Items.DIRT), 5, null);
        view.loadStack(new ItemStack(Items.GLASS), 1, null);
        view.loadStack(new ItemStack(Items.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sorting_descending(GridSorter sorter) {
        // Arrange
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10, null);
        view.loadStack(new ItemStack(Items.DIRT), 5, null);
        view.loadStack(new ItemStack(Items.GLASS), 1, null);
        view.loadStack(new ItemStack(Items.BUCKET), 2, null);

        // Act
        view.sort();

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case NAME:
                assertOrderedItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 2)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemGridStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 2)
                );
                break;
            default:
                fail();
        }
    }

    @Test
    void Test_loading_stack_with_storage_tracker_entry() {
        // Act
        view.loadStack(new ItemStack(Items.DIRT), 1, new StorageTracker.Entry(1, "Raoul"));
        view.loadStack(new ItemStack(Items.DIRT), 1, new StorageTracker.Entry(2, "RaoulA"));

        view.loadStack(new ItemStack(Items.GLASS), 1, new StorageTracker.Entry(3, "VDB"));
        view.loadStack(new ItemStack(Items.GLASS), 1, null);

        view.loadStack(new ItemStack(Items.SPONGE), 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry(new ItemStack(Items.DIRT));
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry(new ItemStack(Items.GLASS));
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry(new ItemStack(Items.SPONGE));

        assertThat(dirt).isPresent();
        assertThat(dirt.get().getName()).isEqualTo("RaoulA");
        assertThat(dirt.get().getTime()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_addition_for_new_stack() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 12), new ItemStack(Items.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_filtering() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.DIRT);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setPreventSorting(true);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 12), new ItemStack(Items.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 6, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 11), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_filtering() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 6, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_but_hidden_stack_when_filtering() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 6, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 6, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 6), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.GLASS), 5, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 11), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 11), new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_sending_change_should_set_storage_tracker_entry() {
        // Act
        view.onChange(new ItemStack(Items.DIRT), 1, new StorageTracker.Entry(1, "Raoul"));
        view.onChange(new ItemStack(Items.DIRT), 1, new StorageTracker.Entry(2, "RaoulA"));

        view.onChange(new ItemStack(Items.GLASS), 1, new StorageTracker.Entry(3, "VDB"));
        view.onChange(new ItemStack(Items.GLASS), 1, null);

        view.onChange(new ItemStack(Items.SPONGE), 1, null);

        // Assert
        Optional<StorageTracker.Entry> dirt = view.getTrackerEntry(new ItemStack(Items.DIRT));
        Optional<StorageTracker.Entry> glass = view.getTrackerEntry(new ItemStack(Items.GLASS));
        Optional<StorageTracker.Entry> sponge = view.getTrackerEntry(new ItemStack(Items.SPONGE));

        assertThat(dirt).isPresent();
        assertThat(dirt.get().getName()).isEqualTo("RaoulA");
        assertThat(dirt.get().getTime()).isEqualTo(2);

        assertThat(glass).isEmpty();
        assertThat(sponge).isEmpty();
    }

    @Test
    void Test_sending_removal() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 13), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_when_filtering() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_for_hidden_stack_when_filtering() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_removal_when_preventing_sort() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.GLASS), -7, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 13));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 13), new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_sending_complete_removal() {
        // Arrange
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -20, null);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_complete_removal_when_preventing_sort() {
        // Arrange
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        view.setPreventSorting(true);
        view.onChange(new ItemStack(Items.GLASS), -20, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        assertThat(view.getStacks()).anyMatch(stack -> stack.getStack().getItem() == Items.GLASS && stack.isZeroed());

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_sending_complete_removal_and_reinserting_stack_should_reuse_same_stack_when_preventing_sort() {
        // Arrange
        view.loadStack(new ItemStack(Items.DIRT), 15, null);
        view.loadStack(new ItemStack(Items.GLASS), 20, null);
        view.loadStack(new ItemStack(Items.SPONGE), 10, null);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        // Delete the item
        view.setPreventSorting(true);
        view.onChange(new ItemStack(Items.GLASS), -20, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));
        assertThat(view.getStacks()).anyMatch(stack -> stack.getStack().getItem() == Items.GLASS && stack.isZeroed());

        // Re-insert the item
        view.onChange(new ItemStack(Items.GLASS), 5, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 5));
        assertThat(view.getStacks()).noneMatch(GridStack::isZeroed);

        // Re-insert the item again
        view.onChange(new ItemStack(Items.GLASS), 3, null);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 8));
        assertThat(view.getStacks()).noneMatch(GridStack::isZeroed);
    }
}

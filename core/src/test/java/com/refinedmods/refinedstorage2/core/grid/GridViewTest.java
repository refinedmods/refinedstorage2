package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertOrderedItemStackListContents;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@RefinedStorage2Test
class GridViewTest {
    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sorting_ascending(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        // Act
        view.sort();

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
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
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        // Act
        view.sort();

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 2)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
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

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_addition_with_ascending_sorting_direction_and_item_already_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.BUCKET), 500);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.BUCKET, 502)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 502),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 502),
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
    void Test_sending_addition_with_descending_sorting_direction_and_item_already_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.DIRT), 500);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.DIRT, 515),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.DIRT, 515),
                    new ItemStack(Items.BUCKET, 2)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 515),
                    new ItemStack(Items.GLASS, 1)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_addition_with_ascending_sorting_direction_and_item_not_yet_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.SPONGE), 3);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.SPONGE, 3),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.SPONGE, 3)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.SPONGE, 3)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_addition_twice_with_ascending_sorting_direction_and_item_not_yet_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 1);
        view.loadStack(new ItemStack(Items.BUCKET), 2);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.SPONGE), 3);
        view.onChange(new ItemStack(Items.SPONGE), 1);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.SPONGE, 4),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.SPONGE, 4)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 2),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 1),
                    new ItemStack(Items.SPONGE, 4)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_deletion_with_ascending_sorting_direction_and_item_already_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.BUCKET), 5);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.GLASS), -3);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 3),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 3)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 3)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_deletion_with_ascending_sorting_direction_and_item_not_yet_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.BUCKET), 5);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.SPONGE), 10);
        view.onChange(new ItemStack(Items.SPONGE), -8);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.SPONGE, 2),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.GLASS, 6),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 6),
                    new ItemStack(Items.SPONGE, 2)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.BUCKET, 5),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 6),
                    new ItemStack(Items.SPONGE, 2)
                );
                break;
            default:
                fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sending_complete_deletion_with_ascending_sorting_direction_and_item_already_present(GridSorter sorter) {
        // Arrange
        GridView view = new GridView();
        view.setSorter(sorter.getComparator());
        view.setSortingDirection(GridSortingDirection.ASCENDING);

        view.loadStack(new ItemStack(Items.DIRT), 10);
        view.loadStack(new ItemStack(Items.DIRT), 5);
        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.BUCKET), 5);

        view.sort();

        // Act
        view.onChange(new ItemStack(Items.BUCKET), -5);

        // Assert
        switch (sorter) {
            case QUANTITY:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.GLASS, 6),
                    new ItemStack(Items.DIRT, 15)
                );
                break;
            case NAME:
                assertOrderedItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 6)
                );
                break;
            case ID:
                // Intended as unordered assert - we don't know the IDs before hand
                assertItemStackListContents(
                    view.getStacks(),
                    new ItemStack(Items.DIRT, 15),
                    new ItemStack(Items.GLASS, 6)
                );
                break;
            default:
                fail();
        }
    }

    @Test
    void Test_filter_should_filter_out_changes() {
        // Arrange
        GridView view = new GridView();
        view.sort();

        // Act & assert
        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.GLASS), 10);

        assertItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 10), new ItemStack(Items.GLASS, 10));

        view.setFilter(stack -> stack.getItem() == Items.DIRT);
        view.sort();

        assertItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 10));

        view.onChange(new ItemStack(Items.DIRT), 5);
        view.onChange(new ItemStack(Items.GLASS), 2);

        assertItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 15));

        view.setFilter(stack -> true);
        view.sort();

        assertItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 12));
    }

    @Test
    void Test_listener_should_be_called_when_sorting() {
        // Arrange
        GridView view = new GridView();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.sort();

        // Assert
        verify(listener, times(1)).run();
    }

    @Test
    void Test_listener_should_be_called_when_applying_change() {
        // Arrange
        GridView view = new GridView();
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.DIRT), -5);

        // Assert
        verify(listener, times(2)).run();
    }

    @Test
    void Test_listener_should_not_be_called_when_applying_change_for_a_stack_that_is_not_visible() {
        // Arrange
        GridView view = new GridView();
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getItem() == Items.DIRT);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.DIRT), -5);

        view.onChange(new ItemStack(Items.SPONGE), 10);
        view.onChange(new ItemStack(Items.SPONGE), -5);

        // Assert
        verify(listener, times(2)).run();
    }

    @Test
    void Test_when_preventing_sorting_sending_addition_should_not_reorder_stacks_if_changed_stack_already_exists() {
        // Arrange
        GridView view = new GridView();
        view.setSorter(GridSorter.QUANTITY.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);
        view.sort();

        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.GLASS), 15);

        // Act & assert
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.DIRT, 10));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.DIRT), 8);
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.DIRT, 18));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 18), new ItemStack(Items.GLASS, 15));
    }

    @Test
    void Test_when_preventing_sorting_sending_addition_should_reorder_stacks_if_changed_stack_does_not_exists() {
        // Arrange
        GridView view = new GridView();
        view.setSorter(GridSorter.QUANTITY.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);
        view.sort();

        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.GLASS), 15);

        // Act & assert
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.DIRT, 10));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.SPONGE), 12);
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.SPONGE, 12), new ItemStack(Items.DIRT, 10));
    }

    @Test
    void Test_when_preventing_sorting_sending_removal_should_not_reorder_stacks() {
        // Arrange
        GridView view = new GridView();
        view.setSorter(GridSorter.QUANTITY.getComparator());
        view.setSortingDirection(GridSortingDirection.DESCENDING);
        view.sort();

        view.onChange(new ItemStack(Items.DIRT), 10);
        view.onChange(new ItemStack(Items.GLASS), 15);

        // Act & assert
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.DIRT, 10));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.GLASS), -8);
        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 7), new ItemStack(Items.DIRT, 10));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 10), new ItemStack(Items.GLASS, 7));
    }
}

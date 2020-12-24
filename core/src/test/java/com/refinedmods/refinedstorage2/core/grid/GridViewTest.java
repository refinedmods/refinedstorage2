package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemGridStackListContents;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertOrderedItemGridStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@RefinedStorage2Test
public class GridViewTest {
    @ParameterizedTest
    @EnumSource(GridSorter.class)
    void Test_sorting_ascending(GridSorter sorter) {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());
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
        GridView view = new GridView(new FakeGridStackFactory());
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
    void Test_sending_addition_for_new_stack() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 12), new ItemStack(Items.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_filtering() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.DIRT);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_new_stack_when_preventing_sort() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setPreventSorting(true);

        // Act
        view.onChange(new ItemStack(Items.DIRT), 12);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 12), new ItemStack(Items.GLASS, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 11), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_filtering() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_addition_for_existing_but_hidden_stack_when_filtering() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), 5);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_addition_for_existing_stack_when_preventing_sort() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 6);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 6), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.GLASS), 5);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.GLASS, 11), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 11), new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_sending_removal() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 13), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_when_filtering() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_removal_for_hidden_stack_when_filtering() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.setFilter(stack -> stack.getStack().getItem() != Items.GLASS);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -7);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, never()).run();
    }

    @Test
    void Test_sending_removal_when_preventing_sort() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        view.setPreventSorting(true);

        view.onChange(new ItemStack(Items.GLASS), -7);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 13));

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.GLASS, 13), new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_sending_complete_removal() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(new ItemStack(Items.GLASS), -20);

        // Assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
        verify(listener, times(1)).run();
    }

    @Test
    void Test_sending_complete_removal_when_preventing_sort() {
        // Arrange
        GridView view = new GridView(new FakeGridStackFactory());

        view.loadStack(new ItemStack(Items.DIRT), 15);
        view.loadStack(new ItemStack(Items.GLASS), 20);
        view.loadStack(new ItemStack(Items.SPONGE), 10);
        view.sort();

        Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        view.setPreventSorting(true);
        view.onChange(new ItemStack(Items.GLASS), -20);
        verify(listener, never()).run();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15), new ItemStack(Items.GLASS, 20));

        assertThat(view.getStacks()).anyMatch(stack -> stack.getStack().getItem() == Items.GLASS && stack.isZeroed());

        view.setPreventSorting(false);
        view.sort();

        assertOrderedItemGridStackListContents(view.getStacks(), new ItemStack(Items.SPONGE, 10), new ItemStack(Items.DIRT, 15));
    }
}

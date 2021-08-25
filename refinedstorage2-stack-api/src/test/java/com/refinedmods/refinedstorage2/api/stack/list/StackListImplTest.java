package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StackListImplTest {
    private final StackList<Rs2ItemStack> list = StackListImpl.createItemStackList();

    @Test
    void Test_adding_a_stack() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        StackListResult<Rs2ItemStack> result = list.add(stack, 10);

        // Assert
        assertThat(result.id()).isNotNull();
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.stack()).isNotSameAs(stack);
        assertThat(result.available()).isTrue();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIRT, 10));
    }

    @Test
    void Test_adding_multiple_stacks_with_same_item() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 5);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 10);
        StackListResult<Rs2ItemStack> result2 = list.add(stack2, 5);

        // Assert
        assertThat(result1.id()).isNotNull();
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result1.available()).isTrue();

        assertThat(result2.id()).isEqualTo(result1.id());
        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.available()).isTrue();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIRT, 15));
    }

    @Test
    void Test_adding_multiple_stacks_with_different_items() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 5);
        Rs2ItemStack stack3 = new Rs2ItemStack(ItemStubs.DIAMOND, 3);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 10);
        StackListResult<Rs2ItemStack> result2 = list.add(stack2, 5);
        StackListResult<Rs2ItemStack> result3 = list.add(stack3, 3);

        // Assert
        assertThat(result1.id()).isNotNull();
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result1.available()).isTrue();

        assertThat(result2.id()).isEqualTo(result1.id());
        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.available()).isTrue();

        assertThat(result3.id()).isEqualTo(result3.id());
        assertThat(result3.change()).isEqualTo(3);
        assertThat(result3.stack()).isNotSameAs(stack3);
        assertThat(result3.available()).isTrue();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.DIAMOND, 3));
    }

    @Test
    void Test_adding_multiple_stacks_with_same_item_but_different_tag() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10, "hello 1");
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 5, "hello 2");
        Rs2ItemStack stack3 = new Rs2ItemStack(ItemStubs.DIAMOND, 3);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 10);
        StackListResult<Rs2ItemStack> result2 = list.add(stack2, 5);
        StackListResult<Rs2ItemStack> result3 = list.add(stack3, 3);

        // Assert
        assertThat(result1.id()).isNotNull();
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result1.available()).isTrue();

        assertThat(result2.id()).isNotNull();
        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.available()).isTrue();

        assertThat(result3.id()).isNotNull();
        assertThat(result3.change()).isEqualTo(3);
        assertThat(result3.stack()).isNotSameAs(stack3);
        assertThat(result3.available()).isTrue();

        Rs2ItemStack expectedStack1 = new Rs2ItemStack(ItemStubs.DIRT, 10, "hello 1");
        Rs2ItemStack expectedStack2 = new Rs2ItemStack(ItemStubs.DIRT, 5, "hello 2");

        assertItemStackListContents(
                list,
                expectedStack1,
                expectedStack2,
                new Rs2ItemStack(ItemStubs.DIAMOND, 3)
        );
    }

    @Test
    void Test_adding_an_empty_stack() {
        // Arrange
        Rs2ItemStack stack = Rs2ItemStack.EMPTY;

        // Act
        Executable action = () -> list.add(stack, 10);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_adding_an_invalid_amount() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        Executable action1 = () -> list.add(stack, 0);
        Executable action2 = () -> list.add(stack, -1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }

    @Test
    void Test_removing_a_stack_when_item_is_not_available() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT, 10);

        // Act
        Optional<StackListResult<Rs2ItemStack>> result = list.remove(stack, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_removing_a_stack_partly() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT);
        Rs2ItemStack stack3 = new Rs2ItemStack(ItemStubs.DIAMOND);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<Rs2ItemStack>> result2 = list.remove(stack2, 5);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-5);
        assertThat(result2.get().stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.get().available()).isTrue();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIRT, 15), new Rs2ItemStack(ItemStubs.DIAMOND, 6));
    }

    @Test
    void Test_removing_a_stack_completely() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 20);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 20);
        Rs2ItemStack stack3 = new Rs2ItemStack(ItemStubs.DIAMOND, 6);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<Rs2ItemStack>> result2 = list.remove(stack2, 20);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-20);
        assertThat(result2.get().stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.get().available()).isFalse();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIAMOND, 6));
    }

    @Test
    void Test_removing_a_stack_with_more_than_is_available() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 20);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 21);
        Rs2ItemStack stack3 = new Rs2ItemStack(ItemStubs.DIAMOND, 6);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<Rs2ItemStack>> result2 = list.remove(stack2, 21);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-20);
        assertThat(result2.get().stack()).isNotSameAs(stack1).isNotSameAs(stack2);
        assertThat(result2.get().available()).isFalse();

        assertItemStackListContents(list, new Rs2ItemStack(ItemStubs.DIAMOND, 6));
    }

    @Test
    void Test_removing_an_empty_stack() {
        // Arrange
        Rs2ItemStack stack = Rs2ItemStack.EMPTY;

        // Act
        Executable action = () -> list.remove(stack, 10);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_removing_an_invalid_amount() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT);

        // Act
        Executable action1 = () -> list.remove(stack, 0);
        Executable action2 = () -> list.remove(stack, -1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }

    @Test
    void Test_adding_a_stack_should_make_it_findable_by_template() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT, 6);

        // Act
        list.add(stack, 6);
        Optional<Rs2ItemStack> stackInList = list.get(stack);

        // Assert
        assertThat(stackInList).isPresent();
        assertItemStack(stackInList.get(), stack);
    }

    @Test
    void Test_adding_a_stack_should_make_it_findable_by_id() {
        // Arrange
        Rs2ItemStack stack = new Rs2ItemStack(ItemStubs.DIRT, 3);

        // Act
        StackListResult<Rs2ItemStack> result = list.add(stack, 3);
        Optional<Rs2ItemStack> stackInList = list.get(result.id());

        // Assert
        assertThat(stackInList).isPresent();
        assertItemStack(stackInList.get(), stack);
    }

    @Test
    void Test_removing_a_stack_partly_should_keep_it_findable_by_id() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 3);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 10);
        list.remove(stack2, 3);

        Optional<Rs2ItemStack> stack1InList = list.get(result1.id());

        // Assert
        assertThat(stack1InList).isPresent();
        assertItemStack(stack1InList.get(), new Rs2ItemStack(ItemStubs.DIRT, 7));
    }

    @Test
    void Test_removing_a_stack_partly_should_keep_it_findable_by_template() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 3);

        // Act
        list.add(stack1, 10);
        list.remove(stack2, 3);

        Optional<Rs2ItemStack> stack1InList = list.get(stack1);

        // Assert
        assertThat(stack1InList).isPresent();
        assertItemStack(stack1InList.get(), new Rs2ItemStack(ItemStubs.DIRT, 7));
    }

    @Test
    void Test_removing_a_stack_completely_should_not_make_it_findable_by_template() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 10);

        // Act
        list.add(stack1, 10);
        list.remove(stack2, 10);

        Optional<Rs2ItemStack> stack1InList = list.get(stack1);

        // Assert
        assertThat(stack1InList).isNotPresent();
    }

    @Test
    void Test_removing_a_stack_completely_should_not_make_it_findable_by_id() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.DIRT, 10);

        // Act
        StackListResult<Rs2ItemStack> result1 = list.add(stack1, 10);
        list.remove(stack2, 10);

        Optional<Rs2ItemStack> stack1InList = list.get(result1.id());

        // Assert
        assertThat(stack1InList).isNotPresent();
    }

    @Test
    void Test_clearing_list() {
        // Arrange
        Rs2ItemStack stack1 = new Rs2ItemStack(ItemStubs.DIRT, 10);
        Rs2ItemStack stack2 = new Rs2ItemStack(ItemStubs.GLASS, 5);

        UUID id1 = list.add(stack1, 10).id();
        UUID id2 = list.add(stack2, 5).id();

        Collection<Rs2ItemStack> listContentsBeforeClear = new ArrayList<>(list.getAll());
        Optional<Rs2ItemStack> stack1ByIdBeforeClear = list.get(id1);
        Optional<Rs2ItemStack> stack2ByIdBeforeClear = list.get(id2);

        // Act
        list.clear();

        Collection<Rs2ItemStack> listContentsAfterClear = list.getAll();
        Optional<Rs2ItemStack> stack1ByIdAfterClear = list.get(id1);
        Optional<Rs2ItemStack> stack2ByIdAfterClear = list.get(id2);

        // Assert
        assertThat(listContentsBeforeClear).hasSize(2);
        assertThat(stack1ByIdBeforeClear).isPresent();
        assertThat(stack2ByIdBeforeClear).isPresent();

        assertThat(listContentsAfterClear).isEmpty();
        assertThat(stack1ByIdAfterClear).isEmpty();
        assertThat(stack2ByIdAfterClear).isEmpty();
    }
}

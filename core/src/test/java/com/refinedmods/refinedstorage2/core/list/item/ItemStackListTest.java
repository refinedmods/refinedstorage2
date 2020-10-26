package com.refinedmods.refinedstorage2.core.list.item;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class ItemStackListTest {
    private final ItemStackList list = new ItemStackList();

    @Test
    void Test_adding_a_stack() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT);

        // Act
        StackListResult<ItemStack> result = list.add(stack, 10);

        // Assert
        assertThat(result.getId()).isNotNull();
        assertThat(result.getChange()).isEqualTo(10);
        assertThat(result.getStack()).isNotSameAs(stack);

        assertItemStackListContents(list, new ItemStack(Items.DIRT, 10));
    }

    @Test
    void Test_adding_multiple_stacks_with_same_item() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 5);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 10);
        StackListResult<ItemStack> result2 = list.add(stack2, 5);

        // Assert
        assertThat(result1.getId()).isNotNull();
        assertThat(result1.getChange()).isEqualTo(10);
        assertThat(result1.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertThat(result2.getId()).isEqualTo(result1.getId());
        assertThat(result2.getChange()).isEqualTo(5);
        assertThat(result2.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStackListContents(list, new ItemStack(Items.DIRT, 15));
    }

    @Test
    void Test_adding_multiple_stacks_with_different_items() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 5);
        ItemStack stack3 = new ItemStack(Items.DIAMOND, 3);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 10);
        StackListResult<ItemStack> result2 = list.add(stack2, 5);
        StackListResult<ItemStack> result3 = list.add(stack3, 3);

        // Assert
        assertThat(result1.getId()).isNotNull();
        assertThat(result1.getChange()).isEqualTo(10);
        assertThat(result1.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertThat(result2.getId()).isEqualTo(result1.getId());
        assertThat(result2.getChange()).isEqualTo(5);
        assertThat(result2.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertThat(result3.getId()).isEqualTo(result3.getId());
        assertThat(result3.getChange()).isEqualTo(3);
        assertThat(result3.getStack()).isNotSameAs(stack3);

        assertItemStackListContents(list, new ItemStack(Items.DIRT, 15), new ItemStack(Items.DIAMOND, 3));
    }

    @Test
    void Test_adding_multiple_stacks_with_same_item_but_different_tag() {
        // Arrange
        CompoundTag tag1 = new CompoundTag();
        tag1.putInt("hello", 1);
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        stack1.setTag(tag1);

        CompoundTag tag2 = new CompoundTag();
        tag2.putInt("hello", 2);
        ItemStack stack2 = new ItemStack(Items.DIRT, 5);
        stack2.setTag(tag2);

        ItemStack stack3 = new ItemStack(Items.DIAMOND, 3);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 10);
        StackListResult<ItemStack> result2 = list.add(stack2, 5);
        StackListResult<ItemStack> result3 = list.add(stack3, 3);

        // Assert
        assertThat(result1.getId()).isNotNull();
        assertThat(result1.getChange()).isEqualTo(10);
        assertThat(result1.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertThat(result2.getId()).isNotNull();
        assertThat(result2.getChange()).isEqualTo(5);
        assertThat(result2.getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertThat(result3.getId()).isNotNull();
        assertThat(result3.getChange()).isEqualTo(3);
        assertThat(result3.getStack()).isNotSameAs(stack3);

        ItemStack expectedStack1 = new ItemStack(Items.DIRT, 10);
        expectedStack1.setTag(tag1.copy());

        ItemStack expectedStack2 = new ItemStack(Items.DIRT, 5);
        expectedStack2.setTag(tag2.copy());

        assertItemStackListContents(
                list,
                expectedStack1,
                expectedStack2,
                new ItemStack(Items.DIAMOND, 3)
        );
    }

    @Test
    void Test_adding_an_empty_stack() {
        // Arrange
        ItemStack stack = ItemStack.EMPTY;

        // Act
        Executable action = () -> list.add(stack, 10);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_adding_an_invalid_amount() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT);

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
        ItemStack stack = new ItemStack(Items.DIRT, 10);

        // Act
        Optional<StackListResult<ItemStack>> result = list.remove(stack, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_removing_a_stack_partly() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT);
        ItemStack stack2 = new ItemStack(Items.DIRT);
        ItemStack stack3 = new ItemStack(Items.DIAMOND);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<ItemStack>> result2 = list.remove(stack2, 5);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().getId()).isEqualTo(result1.getId());
        assertThat(result2.get().getChange()).isEqualTo(-5);
        assertThat(result2.get().getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStackListContents(list, new ItemStack(Items.DIRT, 15), new ItemStack(Items.DIAMOND, 6));
    }

    @Test
    void Test_removing_a_stack_completely() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 20);
        ItemStack stack2 = new ItemStack(Items.DIRT, 20);
        ItemStack stack3 = new ItemStack(Items.DIAMOND, 6);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<ItemStack>> result2 = list.remove(stack2, 20);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().getId()).isEqualTo(result1.getId());
        assertThat(result2.get().getChange()).isEqualTo(-20);
        assertThat(result2.get().getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStackListContents(list, new ItemStack(Items.DIAMOND, 6));
    }

    @Test
    void Test_removing_a_stack_with_more_than_is_available() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 20);
        ItemStack stack2 = new ItemStack(Items.DIRT, 21);
        ItemStack stack3 = new ItemStack(Items.DIAMOND, 6);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 20);
        list.add(stack3, 6);
        Optional<StackListResult<ItemStack>> result2 = list.remove(stack2, 21);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().getId()).isEqualTo(result1.getId());
        assertThat(result2.get().getChange()).isEqualTo(-20);
        assertThat(result2.get().getStack()).isNotSameAs(stack1).isNotSameAs(stack2);

        assertItemStackListContents(list, new ItemStack(Items.DIAMOND, 6));
    }

    @Test
    void Test_removing_an_empty_stack() {
        // Arrange
        ItemStack stack = ItemStack.EMPTY;

        // Act
        Executable action = () -> list.remove(stack, 10);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void Test_removing_an_invalid_amount() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT);

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
        ItemStack stack = new ItemStack(Items.DIRT, 6);

        // Act
        list.add(stack, 6);
        Optional<ItemStack> stackInList = list.get(stack);

        // Assert
        assertThat(stackInList).isPresent();
        assertItemStack(stackInList.get(), stack);
    }

    @Test
    void Test_adding_a_stack_should_make_it_findable_by_id() {
        // Arrange
        ItemStack stack = new ItemStack(Items.DIRT, 3);

        // Act
        StackListResult<ItemStack> result = list.add(stack, 3);
        Optional<ItemStack> stackInList = list.get(result.getId());

        // Assert
        assertThat(stackInList).isPresent();
        assertItemStack(stackInList.get(), stack);
    }

    @Test
    void Test_removing_a_stack_partly_should_keep_it_findable_by_id() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 3);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 10);
        list.remove(stack2, 3);

        Optional<ItemStack> stack1InList = list.get(result1.getId());

        // Assert
        assertThat(stack1InList).isPresent();
        assertItemStack(stack1InList.get(), new ItemStack(Items.DIRT, 7));
    }

    @Test
    void Test_removing_a_stack_partly_should_keep_it_findable_by_template() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 3);

        // Act
        list.add(stack1, 10);
        list.remove(stack2, 3);

        Optional<ItemStack> stack1InList = list.get(stack1);

        // Assert
        assertThat(stack1InList).isPresent();
        assertItemStack(stack1InList.get(), new ItemStack(Items.DIRT, 7));
    }

    @Test
    void Test_removing_a_stack_completely_should_not_make_it_findable_by_template() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 10);

        // Act
        list.add(stack1, 10);
        list.remove(stack2, 10);

        Optional<ItemStack> stack1InList = list.get(stack1);

        // Assert
        assertThat(stack1InList).isNotPresent();
    }

    @Test
    void Test_removing_a_stack_completely_should_not_make_it_findable_by_id() {
        // Arrange
        ItemStack stack1 = new ItemStack(Items.DIRT, 10);
        ItemStack stack2 = new ItemStack(Items.DIRT, 10);

        // Act
        StackListResult<ItemStack> result1 = list.add(stack1, 10);
        list.remove(stack2, 10);

        Optional<ItemStack> stack1InList = list.get(result1.getId());

        // Assert
        assertThat(stack1InList).isNotPresent();
    }
}

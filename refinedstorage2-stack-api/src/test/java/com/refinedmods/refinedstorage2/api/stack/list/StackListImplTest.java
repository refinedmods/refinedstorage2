package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StackListImplTest {
    private StackList<String> list;

    @BeforeEach
    void setUp() {
        list = new StackListImpl<>();
    }

    @Test
    void Test_adding_a_new_resource() {
        // Act
        StackListResult<String> result = list.add("A", 10);

        // Assert
        assertThat(result.id()).isNotNull();
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result.available()).isTrue();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void Test_adding_multiple_with_same_resource() {
        // Act
        StackListResult<String> result1 = list.add("A", 10);
        StackListResult<String> result2 = list.add("A", 5);

        // Assert
        assertThat(result1.id()).isNotNull();
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.resourceAmount().getAmount()).isEqualTo(15);
        assertThat(result1.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result1.available()).isTrue();

        assertThat(result2.id()).isEqualTo(result1.id());
        assertThat(result2.change()).isEqualTo(5);
        assertThat(result1.resourceAmount().getAmount()).isEqualTo(15);
        assertThat(result1.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result2.available()).isTrue();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 15)
        );
    }

    @Test
    void Test_adding_multiple_with_different_resources() {
        // Act
        StackListResult<String> result1 = list.add("A", 10);
        StackListResult<String> result2 = list.add("A", 5);
        StackListResult<String> result3 = list.add("B", 3);

        // Assert
        assertThat(result1.id()).isNotNull();
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.resourceAmount().getAmount()).isEqualTo(15);
        assertThat(result1.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result1.available()).isTrue();

        assertThat(result2.id()).isEqualTo(result1.id());
        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.resourceAmount().getAmount()).isEqualTo(15);
        assertThat(result2.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result2.available()).isTrue();

        assertThat(result3.id()).isEqualTo(result3.id());
        assertThat(result3.change()).isEqualTo(3);
        assertThat(result3.resourceAmount().getAmount()).isEqualTo(3);
        assertThat(result3.resourceAmount().getResource()).isEqualTo("B");
        assertThat(result3.available()).isTrue();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 15),
                new ResourceAmount<>("B", 3)
        );
    }

    @Test
    void Test_adding_an_invalid_amount() {
        // Act
        Executable action1 = () -> list.add("A", 0);
        Executable action2 = () -> list.add("A", -1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }

    @Test
    void Test_removing_a_resource_when_resource_is_not_available() {
        // Act
        Optional<StackListResult<String>> result = list.remove("A", 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Test_removing_a_resource_partly() {
        // Act
        StackListResult<String> result1 = list.add("A", 20);
        list.add("B", 6);
        Optional<StackListResult<String>> result2 = list.remove("A", 5);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-5);
        assertThat(result2.get().resourceAmount().getAmount()).isEqualTo(15);
        assertThat(result2.get().resourceAmount().getResource()).isEqualTo("A");
        assertThat(result2.get().available()).isTrue();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 15),
                new ResourceAmount<>("B", 6)
        );
    }

    @Test
    void Test_removing_a_resource_completely() {
        // Act
        StackListResult<String> result1 = list.add("A", 20);
        list.add("B", 6);
        Optional<StackListResult<String>> result2 = list.remove("A", 20);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-20);
        assertThat(result2.get().resourceAmount().getAmount()).isEqualTo(20);
        assertThat(result2.get().resourceAmount().getResource()).isEqualTo("A");
        assertThat(result2.get().available()).isFalse();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 6)
        );
    }

    @Test
    void Test_removing_a_resource_with_more_than_is_available() {
        // Act
        StackListResult<String> result1 = list.add("A", 20);
        list.add("B", 6);
        Optional<StackListResult<String>> result2 = list.remove("A", 21);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(result1.id());
        assertThat(result2.get().change()).isEqualTo(-20);
        assertThat(result2.get().resourceAmount().getAmount()).isEqualTo(20);
        assertThat(result2.get().resourceAmount().getResource()).isEqualTo("A");
        assertThat(result2.get().available()).isFalse();

        assertThat(list.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 6)
        );
    }

    @Test
    void Test_removing_an_invalid_amount() {
        // Act
        Executable action1 = () -> list.remove("A", 0);
        Executable action2 = () -> list.remove("A", -1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
    }

    @Test
    void Test_adding_a_resource_should_make_it_findable_by_resource() {
        // Arrange
        list.add("A", 6);

        // Act
        Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(6);
    }

    @Test
    void Test_adding_a_resource_should_make_it_findable_by_id() {
        // Arrange
        StackListResult<String> result = list.add("A", 3);

        // Act
        Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(3);
    }

    @Test
    void Test_removing_a_resource_partly_should_keep_it_findable_by_id() {
        // Arrange
        StackListResult<String> result = list.add("A", 10);

        // Act
        list.remove("A", 3);
        Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(7);
    }

    @Test
    void Test_removing_a_resource_partly_should_keep_it_findable_by_resource() {
        // Arrange
        list.add("A", 10);

        // Act
        list.remove("A", 3);
        Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(7);
    }

    @Test
    void Test_removing_a_resource_completely_should_not_make_it_findable_by_resource() {
        // Arrange
        list.add("A", 10);

        // Act
        list.remove("A", 10);
        Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isNotPresent();
    }

    @Test
    void Test_removing_a_resource_completely_should_not_make_it_findable_by_id() {
        // Arrange
        StackListResult<String> result = list.add("A", 10);

        // Act
        list.remove("A", 10);
        Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isNotPresent();
    }

    @Test
    void Test_clearing_list() {
        // Arrange
        UUID id1 = list.add("A", 10).id();
        UUID id2 = list.add("B", 5).id();

        Collection<ResourceAmount<String>> listContentsBeforeClear = new ArrayList<>(list.getAll());
        Optional<ResourceAmount<String>> beforeClear1 = list.get(id1);
        Optional<ResourceAmount<String>> beforeClear2 = list.get(id2);

        // Act
        list.clear();

        Collection<ResourceAmount<String>> listContentsAfterClear = list.getAll();
        Optional<ResourceAmount<String>> afterClear1 = list.get(id1);
        Optional<ResourceAmount<String>> afterClear2 = list.get(id2);

        // Assert
        assertThat(listContentsBeforeClear).hasSize(2);
        assertThat(beforeClear1).isPresent();
        assertThat(beforeClear2).isPresent();

        assertThat(listContentsAfterClear).isEmpty();
        assertThat(afterClear1).isEmpty();
        assertThat(afterClear2).isEmpty();
    }
}

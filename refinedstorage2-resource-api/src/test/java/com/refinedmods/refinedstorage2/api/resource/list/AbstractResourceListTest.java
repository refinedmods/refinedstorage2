package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractResourceListTest {
    private ResourceList<String> list;

    @BeforeEach
    void setUp() {
        list = createList();
    }

    protected abstract ResourceList<String> createList();

    @Test
    void shouldAddNewResource() {
        // Act
        final ResourceListOperationResult<String> result = list.add("A", 10);

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
    void shouldAddNewResourceWithResourceAmountDirectly() {
        // Act
        final ResourceListOperationResult<String> result = list.add(new ResourceAmount<>("A", 10));

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
    void shouldAddMultipleOfSameResource() {
        // Act
        final ResourceListOperationResult<String> result1 = list.add("A", 10);
        final ResourceListOperationResult<String> result2 = list.add("A", 5);

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
    void shouldAddMultipleOfDifferentResources() {
        // Act
        final ResourceListOperationResult<String> result1 = list.add("A", 10);
        final ResourceListOperationResult<String> result2 = list.add("A", 5);
        final ResourceListOperationResult<String> result3 = list.add("B", 3);

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
    @SuppressWarnings("ConstantConditions")
    void shouldNotAddInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> list.add("A", 0);
        final Executable action2 = () -> list.add("A", -1);
        final Executable action3 = () -> list.add(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldNotRemoveResourceWhenItIsNotAvailable() {
        // Act
        final Optional<ResourceListOperationResult<String>> result = list.remove("A", 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRemoveResourcePartly() {
        // Arrange
        final ResourceListOperationResult<String> result1 = list.add("A", 20);
        list.add("B", 6);

        // Act
        final Optional<ResourceListOperationResult<String>> result2 = list.remove("A", 5);

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
    void shouldRemoveResourcePartlyWithResourceAmountDirectly() {
        // Arrange
        final ResourceListOperationResult<String> result1 = list.add("A", 20);
        list.add("B", 6);

        // Act
        final Optional<ResourceListOperationResult<String>> result2 = list.remove(new ResourceAmount<>(
            "A",
            5
        ));

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
    void shouldRemoveResourceCompletely() {
        // Arrange
        final ResourceListOperationResult<String> result1 = list.add("A", 20);
        list.add("B", 6);

        // Act
        final Optional<ResourceListOperationResult<String>> result2 = list.remove("A", 20);

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
    void shouldRemoveResourceCompletelyWithResourceAmountDirectly() {
        // Arrange
        final ResourceListOperationResult<String> result1 = list.add("A", 20);
        list.add("B", 6);

        // Act
        final Optional<ResourceListOperationResult<String>> result2 = list.remove(new ResourceAmount<>(
            "A",
            20
        ));

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
    void shouldNotRemoveResourceWithMoreThanIsAvailable() {
        // Arrange
        final ResourceListOperationResult<String> result1 = list.add("A", 20);
        list.add("B", 6);

        // Act
        final Optional<ResourceListOperationResult<String>> result2 = list.remove("A", 21);

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
    @SuppressWarnings("ConstantConditions")
    void shouldNotRemoveInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> list.remove("A", 0);
        final Executable action2 = () -> list.remove("A", -1);
        final Executable action3 = () -> list.remove(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldBeAbleToRetrieveByResourceAfterAdding() {
        // Arrange
        list.add("A", 6);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(6);
    }

    @Test
    void shouldBeAbleToRetrieveByIdAfterAdding() {
        // Arrange
        final ResourceListOperationResult<String> result = list.add("A", 3);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(3);
    }

    @Test
    void shouldStillBeAbleToRetrieveByIdWhenRemovingPartly() {
        // Arrange
        final ResourceListOperationResult<String> result = list.add("A", 10);
        list.remove("A", 3);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(7);
    }

    @Test
    void shouldStillBeAbleToRetrieveByResourceWhenRemovingPartly() {
        // Arrange
        list.add("A", 10);
        list.remove("A", 3);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isPresent();
        assertThat(resourceAmount.get().getResource()).isEqualTo("A");
        assertThat(resourceAmount.get().getAmount()).isEqualTo(7);
    }

    @Test
    void shouldNotBeAbleToRetrieveByResourceWhenRemovingCompletely() {
        // Arrange
        list.add("A", 10);
        list.remove("A", 10);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get("A");

        // Assert
        assertThat(resourceAmount).isNotPresent();
    }

    @Test
    void shouldNotBeAbleToRetrieveByIdWhenRemovingCompletely() {
        // Arrange
        final ResourceListOperationResult<String> result = list.add("A", 10);
        list.remove("A", 10);

        // Act
        final Optional<ResourceAmount<String>> resourceAmount = list.get(result.id());

        // Assert
        assertThat(resourceAmount).isNotPresent();
    }

    @Test
    void shouldClearList() {
        // Arrange
        final UUID id1 = list.add("A", 10).id();
        final UUID id2 = list.add("B", 5).id();

        final Collection<ResourceAmount<String>> contentsBeforeClear = new ArrayList<>(list.getAll());
        final Optional<ResourceAmount<String>> aBeforeClear = list.get(id1);
        final Optional<ResourceAmount<String>> bBeforeClear = list.get(id2);

        // Act
        list.clear();

        // Assert
        final Collection<ResourceAmount<String>> contentsAfterClear = list.getAll();
        final Optional<ResourceAmount<String>> aAfterClear = list.get(id1);
        final Optional<ResourceAmount<String>> bAfterClear = list.get(id2);

        assertThat(contentsBeforeClear).hasSize(2);
        assertThat(aBeforeClear).isPresent();
        assertThat(bBeforeClear).isPresent();

        assertThat(contentsAfterClear).isEmpty();
        assertThat(aAfterClear).isEmpty();
        assertThat(bAfterClear).isEmpty();
    }
}

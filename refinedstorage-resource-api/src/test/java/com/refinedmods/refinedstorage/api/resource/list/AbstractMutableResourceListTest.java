package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.TestResource;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(InitialStateExtension.class)
abstract class AbstractMutableResourceListTest {

    protected abstract MutableResourceList createList(TestResource[] resources, long amount);

    @Test
    void testInitialState(final MutableResourceList sut) {
        // Assert
        assertThat(sut.copyState()).isEmpty();
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void shouldAddNewResource(final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult result = sut.add(TestResource.A, 10);

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.amount()).isEqualTo(10);
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 10)
        );
        assertThat(sut.get(TestResource.A)).isEqualTo(10);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddNewResourceWithResourceAmountDirectly(final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult result = sut.add(new ResourceAmount(TestResource.A, 10));

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.amount()).isEqualTo(10);
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 10)
        );
        assertThat(sut.get(TestResource.A)).isEqualTo(10);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddMultipleOfSameResource(final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult result1 = sut.add(TestResource.A, 10);
        final MutableResourceList.OperationResult result2 = sut.add(TestResource.A, 5);

        // Assert
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.amount()).isEqualTo(10);
        assertThat(result1.resource()).isEqualTo(TestResource.A);
        assertThat(result1.available()).isTrue();

        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 15)
        );
        assertThat(sut.get(TestResource.A)).isEqualTo(15);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddMultipleOfDifferentResources(final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult result1 = sut.add(TestResource.A, 10);
        final MutableResourceList.OperationResult result2 = sut.add(TestResource.A, 5);
        final MutableResourceList.OperationResult result3 = sut.add(TestResource.B, 3);

        // Assert
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.amount()).isEqualTo(10);
        assertThat(result1.resource()).isEqualTo(TestResource.A);
        assertThat(result1.available()).isTrue();

        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(result3.change()).isEqualTo(3);
        assertThat(result3.amount()).isEqualTo(3);
        assertThat(result3.resource()).isEqualTo(TestResource.B);
        assertThat(result3.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 3)
        );
        assertThat(sut.get(TestResource.A)).isEqualTo(15);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.get(TestResource.B)).isEqualTo(3);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotAddInvalidResourceOrAmount(final MutableResourceList sut) {
        // Act
        final Executable action1 = () -> sut.add(TestResource.A, 0);
        final Executable action2 = () -> sut.add(TestResource.A, -1);
        final Executable action3 = () -> sut.add(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldNotRemoveResourceWhenItIsNotAvailable(final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult result = sut.remove(TestResource.A, 10);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldRemoveResourcePartly(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 20);
        sut.add(TestResource.B, 6);

        // Act
        final MutableResourceList.OperationResult result2 = sut.remove(TestResource.A, 5);

        // Assert
        assertThat(result2).isNotNull();
        assertThat(result2.change()).isEqualTo(-5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(sut.get(TestResource.A)).isEqualTo(15);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.get(TestResource.B)).isEqualTo(6);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    void shouldRemoveResourcePartlyWithResourceAmount(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 20);
        sut.add(TestResource.B, 6);

        // Act
        final MutableResourceList.OperationResult result2 = sut.remove(new ResourceAmount(
            TestResource.A,
            5
        ));

        // Assert
        assertThat(result2).isNotNull();
        assertThat(result2.change()).isEqualTo(-5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(sut.get(TestResource.A)).isEqualTo(15);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.get(TestResource.B)).isEqualTo(6);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    void shouldRemoveResourceCompletely(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 20);
        sut.add(TestResource.B, 6);

        // Act
        final MutableResourceList.OperationResult result = sut.remove(TestResource.A, 20);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.change()).isEqualTo(-20);
        assertThat(result.amount()).isZero();
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isFalse();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(sut.get(TestResource.A)).isZero();
        assertThat(sut.contains(TestResource.A)).isFalse();
        assertThat(sut.get(TestResource.B)).isEqualTo(6);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.B);
    }

    @Test
    void shouldRemoveResourceCompletelyWithResourceAmount(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 20);
        sut.add(TestResource.B, 6);

        // Act
        final MutableResourceList.OperationResult result2 = sut.remove(new ResourceAmount(
            TestResource.A,
            20
        ));

        // Assert
        assertThat(result2).isNotNull();
        assertThat(result2.change()).isEqualTo(-20);
        assertThat(result2.amount()).isZero();
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isFalse();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(sut.get(TestResource.A)).isZero();
        assertThat(sut.contains(TestResource.A)).isFalse();
        assertThat(sut.get(TestResource.B)).isEqualTo(6);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.B);
    }

    @Test
    void shouldRemoveLastResourceOfResourceList(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 1);

        // Act
        final MutableResourceList.OperationResult result = sut.remove(TestResource.A, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.change()).isEqualTo(-1);
        assertThat(result.amount()).isZero();
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isFalse();

        assertThat(sut.copyState()).isEmpty();
        assertThat(sut.get(TestResource.A)).isZero();
        assertThat(sut.contains(TestResource.A)).isFalse();
        assertThat(sut.isEmpty()).isTrue();
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldNotRemoveResourceWithMoreThanIsAvailable(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 20);
        sut.add(TestResource.B, 6);

        // Act
        final MutableResourceList.OperationResult result = sut.remove(TestResource.A, 21);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.change()).isEqualTo(-20);
        assertThat(result.amount()).isZero();
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isFalse();

        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(sut.get(TestResource.A)).isZero();
        assertThat(sut.contains(TestResource.A)).isFalse();
        assertThat(sut.get(TestResource.B)).isEqualTo(6);
        assertThat(sut.contains(TestResource.B)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.B);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotRemoveInvalidResourceOrAmount(final MutableResourceList sut) {
        // Act
        final Executable action1 = () -> sut.remove(TestResource.A, 0);
        final Executable action2 = () -> sut.remove(TestResource.A, -1);
        final Executable action3 = () -> sut.remove(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldClearList(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 10);
        sut.add(TestResource.B, 5);

        final Collection<ResourceAmount> contentsBeforeClear = new ArrayList<>(sut.copyState());

        // Act
        sut.clear();

        // Assert
        final Collection<ResourceAmount> contentsAfterClear = sut.copyState();

        assertThat(contentsBeforeClear).hasSize(2);
        assertThat(contentsAfterClear).isEmpty();

        assertThat(sut.get(TestResource.A)).isZero();
        assertThat(sut.get(TestResource.B)).isZero();
        assertThat(sut.isEmpty()).isTrue();
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldCopyList(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 10);
        sut.add(TestResource.B, 5);

        // Act
        final MutableResourceList copy = sut.copy();

        sut.add(TestResource.A, 1);
        sut.add(TestResource.C, 3);

        copy.add(TestResource.A, 2);
        copy.add(TestResource.D, 3);

        // Assert
        assertThat(sut.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 11),
            new ResourceAmount(TestResource.B, 5),
            new ResourceAmount(TestResource.C, 3)
        );
        assertThat(copy.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 12),
            new ResourceAmount(TestResource.B, 5),
            new ResourceAmount(TestResource.D, 3)
        );
        assertThat(sut.isEmpty()).isFalse();
        assertThat(copy.isEmpty()).isFalse();
    }

    @Test
    void testToString(final MutableResourceList sut) {
        // Arrange
        sut.add(TestResource.A, 10);
        sut.add(TestResource.B, 5);

        // Act
        final String result = sut.toString();

        // Assert
        assertThat(result)
            .startsWith("{")
            .contains("A=10")
            .contains(",")
            .contains("B=5")
            .endsWith("}");
    }
}

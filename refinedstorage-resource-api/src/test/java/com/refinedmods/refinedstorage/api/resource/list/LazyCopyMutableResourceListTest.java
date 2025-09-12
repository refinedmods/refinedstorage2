package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.TestResource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LazyCopyMutableResourceListTest extends AbstractMutableResourceListTest {
    @Override
    protected MutableResourceList createList(final TestResource[] resources, final long amount) {
        final MutableResourceListImpl original = MutableResourceListImpl.create();
        for (final TestResource resource : resources) {
            original.add(resource, amount);
        }
        return LazyCopyMutableResourceList.create(original);
    }

    @Test
    void shouldGetFromOriginal(@InitialState({TestResource.A}) final MutableResourceList sut) {
        // Act + Assert
        assertThat(sut.get(TestResource.A)).isEqualTo(1);
        assertThat(sut.contains(TestResource.A)).isTrue();
        assertThat(sut.isEmpty()).isFalse();
        assertThat(sut.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddCorrectly(@InitialState({TestResource.A}) final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult add = sut.add(TestResource.A, 1);
        // Assert
        assertThat(add.change()).isEqualTo(1);
        assertThat(add.amount()).isEqualTo(2);
        assertThat(add.resource()).isEqualTo(TestResource.A);
        assertThat(add.available()).isTrue();
        assertThat(sut.get(TestResource.A)).isEqualTo(2);
    }

    @Test
    void shouldRemoveCorrectly(
        @InitialState(value = {TestResource.A, TestResource.B, TestResource.C}, amount = 4)
        final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult removeA = sut.remove(TestResource.A, 10);
        final MutableResourceList.OperationResult removeB = sut.remove(TestResource.B, 3);
        final MutableResourceList.OperationResult removeC = sut.remove(TestResource.C, 4);
        // Assert
        assertThat(removeA).isNotNull();
        assertThat(removeA.amount()).isEqualTo(0);
        assertThat(removeA.resource()).isEqualTo(TestResource.A);
        assertThat(removeA.change()).isEqualTo(-4);
        assertThat(removeA.available()).isFalse();
        assertThat(sut.get(TestResource.A)).isEqualTo(0);

        assertThat(removeB).isNotNull();
        assertThat(removeB.amount()).isEqualTo(1);
        assertThat(removeB.change()).isEqualTo(-3);
        assertThat(removeB.resource()).isEqualTo(TestResource.B);
        assertThat(removeB.available()).isTrue();

        assertThat(removeC).isNotNull();
        assertThat(removeC.amount()).isEqualTo(0);
        assertThat(removeC.change()).isEqualTo(-4);
        assertThat(removeC.resource()).isEqualTo(TestResource.C);
        assertThat(removeC.available()).isFalse();
    }

    @Test
    void shouldAddRemoveCorrectlyWithInitial(@InitialState({TestResource.A}) final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult add = sut.add(TestResource.A, 1);
        final MutableResourceList.OperationResult remove = sut.remove(TestResource.A, 3);
        // Assert
        assertThat(add.change()).isEqualTo(1);
        assertThat(add.amount()).isEqualTo(2);
        assertThat(add.available()).isTrue();
        assertThat(remove).isNotNull();
        assertThat(remove.change()).isEqualTo(-2);
        assertThat(remove.amount()).isEqualTo(0);
        assertThat(remove.available()).isFalse();
    }

    @Test
    void shouldRemoveAddCorrectlyWithInitial(@InitialState({TestResource.A}) final MutableResourceList sut) {
        // Act
        final MutableResourceList.OperationResult remove = sut.remove(TestResource.A, 3);
        final MutableResourceList.OperationResult add = sut.add(TestResource.A, 3);
        // Assert
        assertThat(remove).isNotNull();
        assertThat(remove.change()).isEqualTo(-1);
        assertThat(remove.amount()).isEqualTo(0);
        assertThat(remove.available()).isFalse();
        assertThat(add.change()).isEqualTo(3);
        assertThat(add.amount()).isEqualTo(3);
        assertThat(add.available()).isTrue();
    }
}

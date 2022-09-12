package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListenableResourceListTest {
    private FakeResourceListListener<String> listener;
    private ResourceListImpl<String> list;
    private ListenableResourceList<String> sut;

    @BeforeEach
    void setUp() {
        listener = new FakeResourceListListener<>();
        list = new ResourceListImpl<>();
        sut = new ListenableResourceList<>(list);
    }

    @Test
    void shouldCallListenerWhenAdding() {
        // Arrange
        sut.addListener(listener);

        // Act
        final ResourceListOperationResult<String> result = sut.add("A", 10);

        // Assert
        assertThat(result.id()).isNotNull();
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result.available()).isTrue();
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void shouldNotCallListenerWhenAddingWithoutListener() {
        // Act
        final ResourceListOperationResult<String> result = sut.add("A", 10);

        // Assert
        assertThat(result.id()).isNotNull();
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.resourceAmount().getResource()).isEqualTo("A");
        assertThat(result.available()).isTrue();
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldCallListenerWhenRemoving() {
        // Arrange
        sut.addListener(listener);
        sut.add("A", 10);

        // Act
        final Optional<ResourceListOperationResult<String>> result = sut.remove("A", 10);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().id()).isNotNull();
        assertThat(result.get().change()).isEqualTo(-10);
        assertThat(result.get().resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.get().resourceAmount().getResource()).isEqualTo("A");
        assertThat(result.get().available()).isFalse();
        assertThat(listener.changes).hasSize(2);
    }

    @Test
    void shouldNotCallListenerWhenRemovingWithoutListener() {
        // Arrange
        sut.add("A", 10);

        // Act
        final Optional<ResourceListOperationResult<String>> result = sut.remove("A", 10);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().id()).isNotNull();
        assertThat(result.get().change()).isEqualTo(-10);
        assertThat(result.get().resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.get().resourceAmount().getResource()).isEqualTo("A");
        assertThat(result.get().available()).isFalse();
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldNotCallListenerWhenRemovingWithoutResult() {
        // Arrange
        sut.addListener(listener);
        sut.add("A", 10);

        // Act
        final Optional<ResourceListOperationResult<String>> result = sut.remove("B", 10);

        // Assert
        assertThat(result).isEmpty();
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void shouldNotCallListenerWhenModifyingListDirectly() {
        // Arrange
        sut.addListener(listener);

        // Act
        list.add("A", 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldBeAbleToRemoveListener() {
        // Arrange
        sut.addListener(listener);
        sut.add("A", 10);

        // Act
        sut.removeListener(listener);
        sut.add("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    private static class FakeResourceListListener<R> implements ResourceListListener<R> {
        private final List<ResourceListOperationResult<R>> changes = new ArrayList<>();

        @Override
        public void onChanged(final ResourceListOperationResult<R> change) {
            changes.add(change);
        }
    }
}

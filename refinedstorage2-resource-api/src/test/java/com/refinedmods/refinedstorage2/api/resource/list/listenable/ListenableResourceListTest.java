package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.TestResource;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListenableResourceListTest {
    private FakeResourceListListener listener;
    private ResourceListImpl list;
    private ListenableResourceList sut;

    @BeforeEach
    void setUp() {
        listener = new FakeResourceListListener();
        list = new ResourceListImpl();
        sut = new ListenableResourceList(list);
    }

    @Test
    void shouldCallListenerWhenAdding() {
        // Arrange
        sut.addListener(listener);

        // Act
        final ResourceListOperationResult result = sut.add(TestResource.A, 10);

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.resourceAmount().getResource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void shouldNotCallListenerWhenAddingWithoutListener() {
        // Act
        final ResourceListOperationResult result = sut.add(TestResource.A, 10);

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.resourceAmount().getResource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldCallListenerWhenRemoving() {
        // Arrange
        sut.addListener(listener);
        sut.add(TestResource.A, 10);

        // Act
        final Optional<ResourceListOperationResult> result = sut.remove(TestResource.A, 10);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().change()).isEqualTo(-10);
        assertThat(result.get().resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.get().resourceAmount().getResource()).isEqualTo(TestResource.A);
        assertThat(result.get().available()).isFalse();
        assertThat(listener.changes).hasSize(2);
    }

    @Test
    void shouldNotCallListenerWhenRemovingWithoutListener() {
        // Arrange
        sut.add(TestResource.A, 10);

        // Act
        final Optional<ResourceListOperationResult> result = sut.remove(TestResource.A, 10);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().change()).isEqualTo(-10);
        assertThat(result.get().resourceAmount().getAmount()).isEqualTo(10);
        assertThat(result.get().resourceAmount().getResource()).isEqualTo(TestResource.A);
        assertThat(result.get().available()).isFalse();
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldNotCallListenerWhenRemovingWithoutResult() {
        // Arrange
        sut.addListener(listener);
        sut.add(TestResource.A, 10);

        // Act
        final Optional<ResourceListOperationResult> result = sut.remove(TestResource.B, 10);

        // Assert
        assertThat(result).isEmpty();
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void shouldNotCallListenerWhenModifyingListDirectly() {
        // Arrange
        sut.addListener(listener);

        // Act
        list.add(TestResource.A, 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void shouldBeAbleToRemoveListener() {
        // Arrange
        sut.addListener(listener);
        sut.add(TestResource.A, 10);

        // Act
        sut.removeListener(listener);
        sut.add(TestResource.A, 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    private static class FakeResourceListListener implements ResourceListListener {
        private final List<ResourceListOperationResult> changes = new ArrayList<>();

        @Override
        public void onChanged(final ResourceListOperationResult change) {
            changes.add(change);
        }
    }
}

package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
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
    void Test_should_call_listener_when_adding() {
        // Arrange
        sut.addListener(listener);

        // Act
        sut.add("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void Test_should_not_call_listener_when_adding_without_listener() {
        // Act
        sut.add("A", 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void Test_should_call_listener_when_removing() {
        // Arrange
        sut.addListener(listener);
        sut.add("A", 10);

        // Act
        sut.remove("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(2);
    }

    @Test
    void Test_should_not_call_listener_when_removing_without_listener() {
        // Arrange
        sut.add("A", 10);

        // Act
        sut.remove("A", 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void Test_should_not_call_listener_when_removing_with_no_result() {
        // Arrange
        sut.addListener(listener);
        sut.add("A", 10);

        // Act
        sut.remove("B", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void Test_should_not_call_listener_when_calling_list_directly() {
        // Act
        sut.addListener(listener);
        list.add("A", 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    @Test
    void Test_should_be_able_to_remove_listener() {
        // Act
        sut.addListener(listener);
        sut.add("A", 10);
        sut.removeListener(listener);
        sut.add("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    private static class FakeResourceListListener<R> implements ResourceListListener<R> {
        private final List<ResourceListOperationResult<R>> changes = new ArrayList<>();

        @Override
        public void onChanged(ResourceListOperationResult<R> change) {
            changes.add(change);
        }
    }
}

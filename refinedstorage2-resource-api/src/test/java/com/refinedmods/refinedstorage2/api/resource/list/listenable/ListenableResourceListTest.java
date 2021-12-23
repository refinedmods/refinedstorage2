package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
public class ListenableResourceListTest {
    private FakeResourceListListener<String> listener;
    private ResourceListImpl<String> list;
    private ListenableResourceList<String> listenable;

    @BeforeEach
    void setUp() {
        listener = new FakeResourceListListener<>();
        list = new ResourceListImpl<>();
        listenable = new ListenableResourceList<>(list, Set.of(listener));
    }

    @Test
    void Test_should_call_listener_when_adding() {
        // Act
        listenable.add("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void Test_should_call_listener_when_removing() {
        // Arrange
        listenable.add("A", 10);

        // Act
        listenable.remove("A", 10);

        // Assert
        assertThat(listener.changes).hasSize(2);
    }

    @Test
    void Test_should_not_call_listener_when_removing_with_no_result() {
        // Arrange
        listenable.add("A", 10);

        // Act
        listenable.remove("B", 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void Test_should_not_call_listener_when_calling_list_directly() {
        // Act
        list.add("A", 10);

        // Assert
        assertThat(listener.changes).isEmpty();
    }

    private static class FakeResourceListListener<R> implements ResourceListListener<R> {
        private final List<ResourceListOperationResult<R>> changes = new ArrayList<>();

        @Override
        public void onChanged(ResourceListOperationResult<R> change) {
            changes.add(change);
        }
    }
}

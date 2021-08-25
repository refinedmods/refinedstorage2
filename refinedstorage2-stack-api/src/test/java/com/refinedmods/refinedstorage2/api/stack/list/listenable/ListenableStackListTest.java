package com.refinedmods.refinedstorage2.api.stack.list.listenable;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
public class ListenableStackListTest {
    @Test
    void Test_should_call_listener_when_adding() {
        // Arrange
        FakeStackListListener listener = new FakeStackListListener();

        StackListImpl<Rs2ItemStackIdentifier, Rs2ItemStack> list = StackListImpl.createItemStackList();
        ListenableStackList<Rs2ItemStack> listenable = new ListenableStackList<>(list, Set.of(listener));

        // Act
        listenable.add(new Rs2ItemStack(ItemStubs.DIRT), 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    @Test
    void Test_should_call_listener_when_removing() {
        // Arrange
        FakeStackListListener listener = new FakeStackListListener();

        StackListImpl<Rs2ItemStackIdentifier, Rs2ItemStack> list = StackListImpl.createItemStackList();
        ListenableStackList<Rs2ItemStack> listenable = new ListenableStackList<>(list, Set.of(listener));

        listenable.add(new Rs2ItemStack(ItemStubs.DIRT), 10);

        // Act
        listenable.remove(new Rs2ItemStack(ItemStubs.DIRT), 10);

        // Assert
        assertThat(listener.changes).hasSize(2);
    }

    @Test
    void Test_should_not_call_listener_when_removing_with_no_result() {
        // Arrange
        FakeStackListListener listener = new FakeStackListListener();

        StackListImpl<Rs2ItemStackIdentifier, Rs2ItemStack> list = StackListImpl.createItemStackList();
        ListenableStackList<Rs2ItemStack> listenable = new ListenableStackList<>(list, Set.of(listener));

        listenable.add(new Rs2ItemStack(ItemStubs.DIRT), 10);

        // Act
        listenable.remove(new Rs2ItemStack(ItemStubs.GLASS), 10);

        // Assert
        assertThat(listener.changes).hasSize(1);
    }

    private static class FakeStackListListener implements StackListListener<Rs2ItemStack> {
        private final List<StackListResult<Rs2ItemStack>> changes = new ArrayList<>();

        @Override
        public void onChanged(StackListResult<Rs2ItemStack> change) {
            changes.add(change);
        }
    }
}

package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collections;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Rs2Test
class ItemStorageChannelTest {
    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_insertion(Action action) {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(new ItemDiskStorage(10)));

        StackListListener<Rs2ItemStack> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<Rs2ItemStack>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.insert(new Rs2ItemStack(ItemStubs.DIRT), 15, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().getChange()).isEqualTo(10);
            assertItemStack(givenStack.getValue().getStack(), new Rs2ItemStack(ItemStubs.DIRT, 10));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_extraction(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.GLASS), 10, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(diskStorage));

        StackListListener<Rs2ItemStack> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<Rs2ItemStack>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.extract(new Rs2ItemStack(ItemStubs.GLASS), 5, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().getChange()).isEqualTo(-5);
            assertItemStack(givenStack.getValue().getStack(), new Rs2ItemStack(ItemStubs.GLASS, 5));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void Test_inserting() {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(new ItemDiskStorage(10)));

        // Act
        channel.insert(new Rs2ItemStack(ItemStubs.DIRT), 5, Action.EXECUTE);
        channel.insert(new Rs2ItemStack(ItemStubs.GLASS), 5, Action.EXECUTE);

        // Assert
        assertItemStackListContents(channel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 5), new Rs2ItemStack(ItemStubs.GLASS, 5));
    }

    @Test
    void Test_extracting() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(100);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(diskStorage));

        // Act
        channel.extract(new Rs2ItemStack(ItemStubs.DIRT), 49, Action.EXECUTE);

        // Assert
        assertItemStackListContents(channel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 1));
    }

    @Test
    void Test_getting_stack() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(100);
        diskStorage.insert(new Rs2ItemStack(ItemStubs.DIRT), 50, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(diskStorage));

        // Act
        Optional<Rs2ItemStack> stack = channel.get(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(stack).isPresent();
        assertItemStack(stack.get(), new Rs2ItemStack(ItemStubs.DIRT, 50));
    }

    @Test
    void Test_getting_non_existent_stack() {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        // Act
        Optional<Rs2ItemStack> stack = channel.get(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(stack).isEmpty();
    }
}

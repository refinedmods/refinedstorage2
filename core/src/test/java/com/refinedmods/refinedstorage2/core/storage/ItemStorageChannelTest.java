package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Optional;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RefinedStorage2Test
class ItemStorageChannelTest {
    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_insertion(Action action) {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(new ItemDiskStorage(10)));

        StackListListener<ItemStack> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<ItemStack>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.insert(new ItemStack(Items.DIRT), 15, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().getChange()).isEqualTo(10);
            assertItemStack(givenStack.getValue().getStack(), new ItemStack(Items.DIRT, 10));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_listener_on_extraction(Action action) {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(10);
        diskStorage.insert(new ItemStack(Items.GLASS), 10, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(diskStorage));

        StackListListener<ItemStack> listener = mock(StackListListener.class);
        channel.addListener(listener);

        ArgumentCaptor<StackListResult<ItemStack>> givenStack = ArgumentCaptor.forClass(StackListResult.class);

        // Act
        channel.extract(new ItemStack(Items.GLASS), 5, action);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).onChanged(givenStack.capture());

            assertThat(givenStack.getValue().getChange()).isEqualTo(-5);
            assertItemStack(givenStack.getValue().getStack(), new ItemStack(Items.GLASS, 5));
        } else {
            verify(listener, never()).onChanged(any());
        }
    }

    @Test
    void Test_inserting() {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(new ItemDiskStorage(10)));

        // Act
        channel.insert(new ItemStack(Items.DIRT), 5, Action.EXECUTE);
        channel.insert(new ItemStack(Items.GLASS), 5, Action.EXECUTE);

        // Assert
        assertItemStackListContents(channel.getStacks(), new ItemStack(Items.DIRT, 5), new ItemStack(Items.GLASS, 5));
    }

    @Test
    void Test_extracting() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(100);
        diskStorage.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(diskStorage));

        // Act
        channel.extract(new ItemStack(Items.DIRT), 49, Action.EXECUTE);

        // Assert
        assertItemStackListContents(channel.getStacks(), new ItemStack(Items.DIRT, 1));
    }

    @Test
    void Test_getting_stack() {
        // Arrange
        ItemDiskStorage diskStorage = new ItemDiskStorage(100);
        diskStorage.insert(new ItemStack(Items.DIRT), 50, Action.EXECUTE);

        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(diskStorage));

        // Act
        Optional<ItemStack> stack = channel.get(new ItemStack(Items.DIRT));

        // Assert
        assertThat(stack).isPresent();
        assertItemStack(stack.get(), new ItemStack(Items.DIRT, 50));
    }

    @Test
    void Test_getting_non_existent_stack() {
        // Arrange
        ItemStorageChannel channel = new ItemStorageChannel();
        channel.setSources(Arrays.asList(new ItemDiskStorage(100)));

        // Act
        Optional<ItemStack> stack = channel.get(new ItemStack(Items.DIRT));

        // Assert
        assertThat(stack).isEmpty();
    }
}

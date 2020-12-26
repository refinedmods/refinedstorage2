package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class GridEventHandlerImplTest {
    private FakeGridInteractor interactor;
    private ItemStorageChannel storageChannel;
    private GridEventHandlerImpl eventHandler;

    @BeforeEach
    void setUp() {
        interactor = new FakeGridInteractor();
        storageChannel = new ItemStorageChannel();
        eventHandler = new GridEventHandlerImpl(storageChannel, interactor);
    }

    @Test
    void Test_inserting_entire_stack_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        interactor.setCursorStack(new ItemStack(Items.DIRT, 25));

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 25));
    }

    @Test
    void Test_inserting_entire_stack_with_remainder_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        interactor.setCursorStack(new ItemStack(Items.DIRT, 31));

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 1));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 30));
    }

    @Test
    void Test_inserting_single_item_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.DIRT), 29, Action.EXECUTE);
        interactor.setCursorStack(new ItemStack(Items.DIRT, 30));

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.SINGLE);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 29));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 30));
    }

    @Test
    void Test_inserting_single_item_with_full_storage_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.DIRT), 30, Action.EXECUTE);
        interactor.setCursorStack(new ItemStack(Items.DIRT, 64));

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.SINGLE);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 64));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 30));
    }

    @Test
    void Test_inserting_invalid_stack_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_item(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(35)));

        storageChannel.insert(new ItemStack(Items.DIRT), 30, Action.EXECUTE);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        switch (mode) {
            case CURSOR_STACK:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 30));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks());
                break;
            case CURSOR_HALF:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 15));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 15));
                break;
            case PLAYER_INVENTORY_STACK:
                assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 30));
                assertItemStackListContents(storageChannel.getStacks());
                break;
        }
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_item_that_has_large_count(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(1000)));

        storageChannel.insert(new ItemStack(Items.DIRT), 300, Action.EXECUTE);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        switch (mode) {
            case CURSOR_STACK:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 64));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 300 - 64));
                break;
            case CURSOR_HALF:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 32));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 300 - 32));
                break;
            case PLAYER_INVENTORY_STACK:
                assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 64));
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 300 - 64));
                break;
        }
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_item_that_is_not_found(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.GLASS), 30, Action.EXECUTE);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(interactor.getInventory());
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 30));
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_item_should_respect_max_stack_size(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.BUCKET), 64, Action.EXECUTE);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.BUCKET), mode);

        // Assert
        switch (mode) {
            case CURSOR_STACK:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.BUCKET, 16));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.BUCKET, 64 - 16));
                break;
            case CURSOR_HALF:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.BUCKET, 8));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.BUCKET, 64 - 8));
                break;
            case PLAYER_INVENTORY_STACK:
                assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.BUCKET, 16));
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.BUCKET, 64 - 16));
                break;
        }
    }

    @Test
    void Test_extracting_item_when_inventory_is_full_should_return_to_storage() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 64, Action.EXECUTE);

        interactor.setFull(true);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT, 32), GridExtractMode.PLAYER_INVENTORY_STACK);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 64));
        assertItemStackListContents(interactor.getInventory());
    }

    @Test
    void Test_extracting_half_single_item_should_extract_single_item() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 1, Action.EXECUTE);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), GridExtractMode.CURSOR_HALF);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 1));
        assertItemStackListContents(interactor.getInventory());
        assertItemStackListContents(storageChannel.getStacks());
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_from_cursor_should_not_perform_when_cursor_already_has_stack(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 65, Action.EXECUTE);

        interactor.setCursorStack(new ItemStack(Items.DIRT, 3));

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        switch (mode) {
            case CURSOR_STACK:
            case CURSOR_HALF:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 3));
                assertItemStackListContents(interactor.getInventory());
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 65));
                break;
            case PLAYER_INVENTORY_STACK:
                assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 3));
                assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 64));
                assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 1));
                break;
        }
    }

    @Test
    void Test_extracting_single_stack_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        // Act
        eventHandler.onScrollInGrid(new ItemStack(Items.DIRT), ScrollInGridMode.EXTRACT_SINGLE_STACK_FROM_GRID);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 31));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 1));
    }

    @Test
    void Test_extracting_single_stack_that_does_not_exist_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        // Act
        eventHandler.onScrollInGrid(new ItemStack(Items.GLASS), ScrollInGridMode.EXTRACT_SINGLE_STACK_FROM_GRID);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 32));
        assertItemStackListContents(interactor.getInventory());
    }

    @Test
    void Test_extracting_single_stack_that_has_no_space_in_inventory_should_return_to_storage_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        interactor.setFull(true);

        // Act
        eventHandler.onScrollInGrid(new ItemStack(Items.DIRT), ScrollInGridMode.EXTRACT_SINGLE_STACK_FROM_GRID);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 32));
        assertItemStackListContents(interactor.getInventory());
    }
}

package com.refinedmods.refinedstorage2.core.grid;

import java.util.Collections;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
        eventHandler = new GridEventHandlerImpl(true, new StorageTrackerEntryPresentAssertionItemStorageChannel(storageChannel), interactor);
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_inserting_entire_stack_with_no_space_left_in_storage_from_cursor() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.GLASS), 30, Action.EXECUTE);

        interactor.setCursorStack(new ItemStack(Items.DIRT, 1));

        // Act
        eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 1));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 30));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(GridInsertMode.class)
    void Test_inserting_from_cursor_when_inactive(GridInsertMode insertMode) {
        // Arrange
        eventHandler.onActiveChanged(false);

        interactor.setCursorStack(new ItemStack(Items.DIRT, 64));

        // Act
        eventHandler.onInsertFromCursor(insertMode);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 64));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isNotPresent();
    }

    @Test
    void Test_inserting_by_transferring() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        Slot slot = new Slot(new SimpleInventory(1), 0, 0, 0);
        slot.setStack(new ItemStack(Items.GLASS, 30));

        // Act
        ItemStack resultingStack = eventHandler.onInsertFromTransfer(new ItemStack(Items.GLASS, 30));

        // Assert
        assertThat(resultingStack.isEmpty()).isTrue();
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 30));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_inserting_by_transferring_with_remainder() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.GLASS), 15, Action.EXECUTE);

        // Act
        ItemStack resultingStack = eventHandler.onInsertFromTransfer(new ItemStack(Items.DIRT, 64));

        // Assert
        assertItemStack(resultingStack, new ItemStack(Items.DIRT, 64 - 15));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 15), new ItemStack(Items.DIRT, 15));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_inserting_by_transferring_when_storage_is_full() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(30)));

        storageChannel.insert(new ItemStack(Items.GLASS), 30, Action.EXECUTE);

        // Act
        ItemStack resultingStack = eventHandler.onInsertFromTransfer(new ItemStack(Items.DIRT, 64));

        // Assert
        assertItemStack(resultingStack, new ItemStack(Items.DIRT, 64));
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 30));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_inserting_by_transferring_when_inactive() {
        // Arrange
        eventHandler.onActiveChanged(false);

        // Act
        ItemStack resultingStack = eventHandler.onInsertFromTransfer(new ItemStack(Items.DIRT, 64));

        // Assert
        assertItemStack(resultingStack, new ItemStack(Items.DIRT, 64));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isNotPresent();
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.BUCKET));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_item_when_inventory_is_full_after_insert_should_return_remainder_to_storage() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 64, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(20);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT, 32), GridExtractMode.PLAYER_INVENTORY_STACK);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 64 - 20));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 20));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_item_when_inventory_is_full_before_insert_should_return_everything_to_storage() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 64, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(20);
        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 20), -1, Action.EXECUTE);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT, 32), GridExtractMode.PLAYER_INVENTORY_STACK);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 64));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 20));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
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

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @ParameterizedTest
    @EnumSource(value = GridExtractMode.class, names = {"CURSOR_STACK", "CURSOR_HALF"})
    void Test_extracting_from_cursor_should_not_perform_when_cursor_already_has_stack(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.DIRT), 65, Action.EXECUTE);

        interactor.setCursorStack(new ItemStack(Items.DIRT, 3));

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        assertItemStack(interactor.getCursorStack(), new ItemStack(Items.DIRT, 3));
        assertItemStackListContents(interactor.getInventory());
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 65));
    }

    @ParameterizedTest
    @EnumSource(GridExtractMode.class)
    void Test_extracting_item_when_inactive(GridExtractMode mode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(35)));
        storageChannel.insert(new ItemStack(Items.DIRT), 30, Action.EXECUTE);

        eventHandler.onActiveChanged(false);

        interactor.setCursorStack(ItemStack.EMPTY);

        // Act
        eventHandler.onExtract(new ItemStack(Items.DIRT), mode);

        // Assert
        assertThat(interactor.getCursorStack().isEmpty()).isTrue();
        assertItemStackListContents(interactor.getInventory());
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 30));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isNotPresent();
    }

    @Test
    void Test_extracting_single_stack_from_grid_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(32);
        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 20), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 31));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 20), new ItemStack(Items.DIRT, 1));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_single_stack_from_grid_that_does_not_exist_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 32));
        assertItemStackListContents(interactor.getInventory());

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_single_stack_from_grid_that_has_no_space_in_inventory_should_return_to_storage_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(32);
        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 32), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 32));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 32));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_grid_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(300)));
        storageChannel.insert(new ItemStack(Items.DIRT), 129, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 65));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 64));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_stack_from_grid_that_does_not_exist_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 32, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.GRID_TO_INVENTORY_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 32));
        assertItemStackListContents(interactor.getInventory());

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_grid_that_has_no_space_in_inventory_after_insert_should_return_remainder_to_storage_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(300)));
        storageChannel.insert(new ItemStack(Items.DIRT), 300, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(32);
        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 20), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 300 - 12));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 20), new ItemStack(Items.DIRT, 12));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_stack_from_grid_that_has_no_space_in_inventory_before_insert_should_return_remainder_to_storage_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(300)));
        storageChannel.insert(new ItemStack(Items.DIRT), 300, Action.EXECUTE);

        interactor.resetInventoryAndSetCapacity(32);
        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 32), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 300));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 32));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_grid_should_respect_max_stack_size_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(300)));
        storageChannel.insert(new ItemStack(Items.BUCKET), 300, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.BUCKET), -1, GridScrollMode.GRID_TO_INVENTORY_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.BUCKET, 300 - 16));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.BUCKET, 16));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.BUCKET));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @ParameterizedTest
    @EnumSource(value = GridScrollMode.class, names = {"GRID_TO_INVENTORY_SINGLE_STACK", "GRID_TO_INVENTORY_STACK"})
    void Test_extracting_stack_from_grid_by_scrolling_when_inactive_in_grid(GridScrollMode scrollMode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));
        storageChannel.insert(new ItemStack(Items.DIRT), 10, Action.EXECUTE);

        eventHandler.onActiveChanged(false);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), 1, scrollMode);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 10));
        assertItemStackListContents(interactor.getInventory());

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isNotPresent();
    }

    @Test
    void Test_extracting_single_stack_from_inventory_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 128), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 1));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 127));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_single_stack_from_inventory_that_does_not_exist_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.DIRT, 128), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks());
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 128));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_single_stack_from_inventory_that_has_no_space_in_storage_should_return_to_inventory_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(2)));
        storageChannel.insert(new ItemStack(Items.DIRT), 2, Action.EXECUTE);

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 128), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.DIRT, 2));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 128));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_inventory_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 129), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.GLASS, 64));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 65));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_stack_from_inventory_that_does_not_exist_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 129), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, GridScrollMode.INVENTORY_TO_GRID_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks());
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 129));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_inventory_that_has_no_space_in_storage_after_insert_should_return_remainder_to_inventory_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.SPONGE), 60, Action.EXECUTE);

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 129), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.SPONGE, 60), new ItemStack(Items.GLASS, 40));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 129 - 40));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @Test
    void Test_extracting_stack_from_inventory_that_has_no_space_in_storage_before_insert_should_return_remainder_to_inventory_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        storageChannel.insert(new ItemStack(Items.SPONGE), 100, Action.EXECUTE);

        interactor.insertIntoInventory(new ItemStack(Items.GLASS, 129), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.SPONGE, 100));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.GLASS, 129));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.GLASS));
        assertThat(entry).isEmpty();
    }

    @Test
    void Test_extracting_stack_from_inventory_should_respect_max_stack_size_by_scrolling_in_grid() {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.BUCKET, 129), -1, Action.EXECUTE);

        // Act
        eventHandler.onScroll(new ItemStack(Items.BUCKET), -1, GridScrollMode.INVENTORY_TO_GRID_STACK);

        // Assert
        assertItemStackListContents(storageChannel.getStacks(), new ItemStack(Items.BUCKET, 16));
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.BUCKET, 129 - 16));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.BUCKET));
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo(FakeGridInteractor.NAME);
    }

    @ParameterizedTest
    @EnumSource(value = GridScrollMode.class, names = {"INVENTORY_TO_GRID_SINGLE_STACK", "INVENTORY_TO_GRID_STACK"})
    void Test_extracting_stack_from_inventory_by_scrolling_when_inactive_in_grid(GridScrollMode scrollMode) {
        // Arrange
        storageChannel.setSources(Collections.singletonList(new ItemDiskStorage(100)));

        interactor.insertIntoInventory(new ItemStack(Items.DIRT, 10), -1, Action.EXECUTE);

        eventHandler.onActiveChanged(false);

        // Act
        eventHandler.onScroll(new ItemStack(Items.DIRT), -1, scrollMode);

        // Assert
        assertItemStackListContents(storageChannel.getStacks());
        assertItemStackListContents(interactor.getInventory(), new ItemStack(Items.DIRT, 10));

        Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new ItemStack(Items.DIRT));
        assertThat(entry).isNotPresent();
    }
}

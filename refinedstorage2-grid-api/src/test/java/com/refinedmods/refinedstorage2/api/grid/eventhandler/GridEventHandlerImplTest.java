package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStack;
import static com.refinedmods.refinedstorage2.api.stack.test.ItemStackAssertions.assertItemStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridEventHandlerImplTest {
    private FakeGridInteractor interactor;
    private StorageChannel<Rs2ItemStack> storageChannel;
    private GridEventHandlerImpl eventHandler;

    @BeforeEach
    void setUp() {
        interactor = new FakeGridInteractor();
        storageChannel = StorageChannelTypes.ITEM.create();
        eventHandler = new GridEventHandlerImpl(true, new StorageTrackerEntryPresentAssertionItemStorageChannel<>(storageChannel), interactor);
    }

    @Test
    void Test_activeness() {
        // Assert
        assertThat(eventHandler.isActive()).isTrue();
    }

    @Nested
    class InsertingEntireStack {
        @Test
        void Test_inserting_entire_stack_from_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 25));

            // Act
            eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 25));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_inserting_entire_stack_with_remainder_from_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 31));

            // Act
            eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 1));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_inserting_entire_stack_with_no_space_left_in_storage_from_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.GLASS), 30, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 1));

            // Act
            eventHandler.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 1));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }
    }

    @Nested
    class InsertingSingle {
        @Test
        void Test_inserting_single_item_from_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 29, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 30));

            // Act
            eventHandler.onInsertFromCursor(GridInsertMode.SINGLE);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 29));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_inserting_single_item_with_full_storage_from_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 30, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 64));

            // Act
            eventHandler.onInsertFromCursor(GridInsertMode.SINGLE);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 64));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }
    }

    @Nested
    class Inserting {
        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_when_inactive(GridInsertMode insertMode) {
            // Arrange
            eventHandler.onActiveChanged(false);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 64));

            // Act
            eventHandler.onInsertFromCursor(insertMode);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 64));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isNotPresent();

            assertThat(eventHandler.isActive()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_invalid_stack(GridInsertMode insertMode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onInsertFromCursor(insertMode);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(storageChannel.getStacks());
        }
    }

    @Nested
    class InsertingByTransferring {
        @Test
        void Test_inserting_by_transferring() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            // Act
            Rs2ItemStack resultingStack = eventHandler.onInsertFromTransfer(new Rs2ItemStack(ItemStubs.GLASS, 30));

            // Assert
            assertThat(resultingStack.isEmpty()).isTrue();
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_inserting_by_transferring_with_remainder() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.GLASS), 15, Action.EXECUTE);

            // Act
            Rs2ItemStack resultingStack = eventHandler.onInsertFromTransfer(new Rs2ItemStack(ItemStubs.DIRT, 64));

            // Assert
            assertItemStack(resultingStack, new Rs2ItemStack(ItemStubs.DIRT, 64 - 15));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 15), new Rs2ItemStack(ItemStubs.DIRT, 15));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_inserting_by_transferring_when_storage_is_full() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.GLASS), 30, Action.EXECUTE);

            // Act
            Rs2ItemStack resultingStack = eventHandler.onInsertFromTransfer(new Rs2ItemStack(ItemStubs.DIRT, 64));

            // Assert
            assertItemStack(resultingStack, new Rs2ItemStack(ItemStubs.DIRT, 64));
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_inserting_by_transferring_when_inactive() {
            // Arrange
            eventHandler.onActiveChanged(false);

            // Act
            Rs2ItemStack resultingStack = eventHandler.onInsertFromTransfer(new Rs2ItemStack(ItemStubs.DIRT, 64));

            // Assert
            assertItemStack(resultingStack, new Rs2ItemStack(ItemStubs.DIRT, 64));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isNotPresent();

            assertThat(eventHandler.isActive()).isFalse();
        }
    }

    @Nested
    class Extracting {
        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_item(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(35));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 30, Action.EXECUTE);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), mode);

            // Assert
            switch (mode) {
                case CURSOR_STACK -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 30));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks());
                }
                case CURSOR_HALF -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 15));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 15));
                }
                case PLAYER_INVENTORY_STACK -> {
                    assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                    assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.DIRT, 30));
                    assertItemStackListContents(storageChannel.getStacks());
                }
            }

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_item_that_has_large_count(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(1000));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 300, Action.EXECUTE);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), mode);

            // Assert
            switch (mode) {
                case CURSOR_STACK -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 64));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 300 - 64));
                }
                case CURSOR_HALF -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 32));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 300 - 32));
                }
                case PLAYER_INVENTORY_STACK -> {
                    assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                    assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.DIRT, 64));
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 300 - 64));
                }
            }

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_item_that_is_not_found(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(30));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.GLASS), 30, Action.EXECUTE);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), mode);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(interactor.getInventory());
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_item_should_respect_max_stack_size(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.BUCKET), 64, Action.EXECUTE);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.BUCKET), mode);

            // Assert
            switch (mode) {
                case CURSOR_STACK -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.BUCKET, 16));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.BUCKET, 64 - 16));
                }
                case CURSOR_HALF -> {
                    assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.BUCKET, 8));
                    assertItemStackListContents(interactor.getInventory());
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.BUCKET, 64 - 8));
                }
                case PLAYER_INVENTORY_STACK -> {
                    assertThat(interactor.getCursorStack().isEmpty()).isTrue();
                    assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.BUCKET, 16));
                    assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.BUCKET, 64 - 16));
                }
            }

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.BUCKET));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @ParameterizedTest
        @EnumSource(GridExtractMode.class)
        void Test_extracting_item_when_inactive(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(35));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 30, Action.EXECUTE);

            eventHandler.onActiveChanged(false);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), mode);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(interactor.getInventory());
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 30));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isNotPresent();

            assertThat(eventHandler.isActive()).isFalse();
        }
    }

    @Nested
    class ExtractingFromGridToInventory {
        @Test
        void Test_extracting_from_grid_to_inventory_when_inventory_is_full_during_insert_should_return_remainder_to_storage() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 64, Action.EXECUTE);

            interactor.resetInventoryAndSetCapacity(20);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT, 32), GridExtractMode.PLAYER_INVENTORY_STACK);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 64 - 20));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.DIRT, 20));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_extracting_from_grid_to_inventory_when_inventory_is_full_before_insert_should_return_remainder_to_storage() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 64, Action.EXECUTE);

            interactor.resetInventoryAndSetCapacity(20);
            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.GLASS, 20), -1, Action.EXECUTE);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT, 32), GridExtractMode.PLAYER_INVENTORY_STACK);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 64));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.GLASS, 20));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }
    }

    @Nested
    class ExtractingFromGridToCursor {
        @Test
        void Test_extracting_half_single_item_from_grid_to_cursor_should_still_extract_single_item() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 1, Action.EXECUTE);

            interactor.setCursorStack(Rs2ItemStack.EMPTY);

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), GridExtractMode.CURSOR_HALF);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 1));
            assertItemStackListContents(interactor.getInventory());
            assertItemStackListContents(storageChannel.getStacks());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @ParameterizedTest
        @EnumSource(value = GridExtractMode.class, names = {"CURSOR_STACK", "CURSOR_HALF"})
        void Test_extracting_item_from_grid_to_cursor_should_not_perform_when_cursor_already_has_stack(GridExtractMode mode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 65, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, 3));

            // Act
            eventHandler.onExtract(new Rs2ItemStack(ItemStubs.DIRT), mode);

            // Assert
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 3));
            assertItemStackListContents(interactor.getInventory());
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 65));
        }
    }

    @Nested
    class ScrollingFromGridToInventory {
        @Test
        void Test_extracting_from_grid_to_inventory() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            interactor.resetInventoryAndSetCapacity(32);
            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.GLASS, 20), -1, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 31));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.GLASS, 20), new Rs2ItemStack(ItemStubs.DIRT, 1));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_extracting_stack_that_does_not_exist_from_grid_to_inventory() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.GLASS), -1, GridScrollMode.GRID_TO_INVENTORY);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 32));
            assertItemStackListContents(interactor.getInventory());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_stack_that_has_no_space_in_inventory_from_grid_to_inventory_should_return_remainder_to_storage() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            interactor.resetInventoryAndSetCapacity(32);
            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.GLASS, 32), -1, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_INVENTORY);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 32));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.GLASS, 32));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_from_grid_to_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_CURSOR);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 31));
            assertItemStackListContents(interactor.getInventory());
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, 1));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_extracting_stack_that_does_not_exist_from_grid_to_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.GLASS), -1, GridScrollMode.GRID_TO_CURSOR);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 32));
            assertItemStackListContents(interactor.getInventory());
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_from_grid_to_cursor_when_item_is_already_on_cursor() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, ItemStubs.DIRT.getMaxAmount() - 1));

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_CURSOR);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 31));
            assertItemStackListContents(interactor.getInventory());
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, ItemStubs.DIRT.getMaxAmount()));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_extracting_from_grid_to_cursor_when_item_currently_on_cursor_does_not_stack() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.GLASS));

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_CURSOR);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 32));
            assertItemStackListContents(interactor.getInventory());
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.GLASS));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_from_grid_to_cursor_when_item_currently_on_cursor_would_overflow() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 32, Action.EXECUTE);

            interactor.setCursorStack(new Rs2ItemStack(ItemStubs.DIRT, ItemStubs.DIRT.getMaxAmount()));

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.GRID_TO_CURSOR);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 32));
            assertItemStackListContents(interactor.getInventory());
            assertItemStack(interactor.getCursorStack(), new Rs2ItemStack(ItemStubs.DIRT, ItemStubs.DIRT.getMaxAmount()));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isEmpty();
        }
    }

    @Nested
    class ScrollingFromGrid {
        @ParameterizedTest
        @EnumSource(value = GridScrollMode.class, names = {"GRID_TO_INVENTORY", "GRID_TO_CURSOR"})
        void Test_extracting_from_grid_when_inactive(GridScrollMode scrollMode) {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 10, Action.EXECUTE);

            eventHandler.onActiveChanged(false);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), 1, scrollMode);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 10));
            assertItemStackListContents(interactor.getInventory());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isNotPresent();

            assertThat(eventHandler.isActive()).isFalse();
        }
    }

    @Nested
    class ScrollingFromInventoryToGrid {
        @Test
        void Test_extracting_single_stack_from_inventory_to_grid() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.GLASS, 128), -1, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.GLASS, 1));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.GLASS, 127));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isPresent();
            assertThat(entry.get().name()).isEqualTo(FakeGridInteractor.NAME);
        }

        @Test
        void Test_extracting_single_stack_from_inventory_to_grid_that_does_not_exist() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.DIRT, 128), -1, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID);

            // Assert
            assertItemStackListContents(storageChannel.getStacks());
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.DIRT, 128));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_single_stack_from_inventory_to_grid_that_has_no_space_in_storage_should_return_remainder_to_inventory() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(2));
            storageChannel.insert(new Rs2ItemStack(ItemStubs.DIRT), 2, Action.EXECUTE);

            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.GLASS, 128), -1, Action.EXECUTE);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.GLASS), -1, GridScrollMode.INVENTORY_TO_GRID);

            // Assert
            assertItemStackListContents(storageChannel.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 2));
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.GLASS, 128));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.GLASS));
            assertThat(entry).isEmpty();
        }

        @Test
        void Test_extracting_from_inventory_to_grid_when_inactive() {
            // Arrange
            storageChannel.addSource(StorageDiskImpl.createItemStorageDisk(100));

            interactor.insertIntoInventory(new Rs2ItemStack(ItemStubs.DIRT, 10), -1, Action.EXECUTE);

            eventHandler.onActiveChanged(false);

            // Act
            eventHandler.onScroll(new Rs2ItemStack(ItemStubs.DIRT), -1, GridScrollMode.INVENTORY_TO_GRID);

            // Assert
            assertItemStackListContents(storageChannel.getStacks());
            assertItemStackListContents(interactor.getInventory(), new Rs2ItemStack(ItemStubs.DIRT, 10));

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(new Rs2ItemStack(ItemStubs.DIRT));
            assertThat(entry).isNotPresent();

            assertThat(eventHandler.isActive()).isFalse();
        }
    }
}

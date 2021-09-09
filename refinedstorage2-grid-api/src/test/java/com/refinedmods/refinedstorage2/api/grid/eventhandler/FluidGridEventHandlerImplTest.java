package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.test.FluidStubs;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage2.api.stack.test.FluidStackAssertions.assertFluidStack;
import static com.refinedmods.refinedstorage2.api.stack.test.FluidStackAssertions.assertFluidStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class FluidGridEventHandlerImplTest {
    private static final long BUCKET_AMOUNT = 1000;

    private FakeFluidGridInteractor interactor;
    private StorageChannel<Rs2FluidStack> storageChannel;
    private FluidGridEventHandlerImpl sut;

    @BeforeEach
    void setUp() {
        interactor = new FakeFluidGridInteractor(BUCKET_AMOUNT);
        storageChannel = StorageChannelTypes.FLUID.create();
        sut = new FluidGridEventHandlerImpl(interactor, storageChannel, true);
        storageChannel.addSource(StorageDiskImpl.createFluidStorageDisk(10_000));
    }

    @Nested
    class InsertingEntireStack {
        @Test
        void Test_inserting_from_cursor() {
            // Arrange
            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Act
            sut.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 2000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }

        @Test
        void Test_inserting_partially_when_storage_is_full() {
            // Arrange
            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));
            storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 8500, Action.EXECUTE);

            // Act
            sut.onInsertFromCursor(GridInsertMode.ENTIRE_STACK);

            // Assert
            assertFluidStack(interactor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 500));
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }
    }

    @Nested
    class InsertingSingle {
        @Test
        void Test_inserting_from_cursor() {
            // Arrange
            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Act
            sut.onInsertFromCursor(GridInsertMode.SINGLE);

            // Assert
            assertFluidStack(interactor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 2000 - BUCKET_AMOUNT));
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, BUCKET_AMOUNT));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }

        @Test
        void Test_inserting_partially_when_storage_is_full() {
            // Arrange
            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));
            storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 9500, Action.EXECUTE);

            // Act
            sut.onInsertFromCursor(GridInsertMode.SINGLE);

            // Assert
            assertFluidStack(interactor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1500));
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }
    }

    @Nested
    class Inserting {
        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_nothing_when_inactive(GridInsertMode insertMode) {
            // Arrange
            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 1));

            sut.onActiveChanged(false);

            // Act
            sut.onInsertFromCursor(insertMode);

            // Assert
            assertFluidStack(interactor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1));
            assertFluidStackListContents(storageChannel.getStacks());
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_with_invalid_stack(GridInsertMode insertMode) {
            // Arrange
            interactor.setCursorStack(Rs2FluidStack.EMPTY);

            // Act
            sut.onInsertFromCursor(insertMode);

            // Assert
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks());
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(GridInsertMode.class)
        void Test_inserting_nothing_when_storage_is_full(GridInsertMode insertMode) {
            // Arrange
            storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 10_000, Action.EXECUTE);

            interactor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 1));

            // Act
            sut.onInsertFromCursor(insertMode);

            // Assert
            assertFluidStack(interactor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1));
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
        }
    }

    @Nested
    class InsertingByTransferring {
        @Test
        void Test_inserting_by_transferring() {
            // Act
            long remainder = sut.onInsertFromTransfer(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Assert
            assertThat(remainder).isZero();
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 2000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }

        @Test
        void Test_inserting_partial_stack_by_transferring() {
            // Arrange
            storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 9000, Action.EXECUTE);

            // Act
            long remainder = sut.onInsertFromTransfer(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Assert
            assertThat(remainder).isEqualTo(1000);
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
        }

        @Test
        void Test_inserting_nothing_due_to_full_storage_by_transferring() {
            // Arrange
            storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 10_000, Action.EXECUTE);

            // Act
            long remainder = sut.onInsertFromTransfer(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Assert
            assertThat(remainder).isEqualTo(2000);
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
        }

        @Test
        void Test_inserting_by_transferring_when_inactive() {
            // Arrange
            sut.onActiveChanged(false);

            // Act
            long remainder = sut.onInsertFromTransfer(new Rs2FluidStack(FluidStubs.WATER, 2000));

            // Assert
            assertThat(remainder).isEqualTo(2000);
            assertThat(interactor.getCursorStack().isEmpty()).isTrue();
            assertFluidStackListContents(storageChannel.getStacks());
            assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
        }
    }
}

package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.test.FluidStubs;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.stack.test.FluidStackAssertions.assertFluidStack;
import static com.refinedmods.refinedstorage2.api.stack.test.FluidStackAssertions.assertFluidStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class FluidGridEventHandlerImplTest {
    private static final long BUCKET_AMOUNT = 1000;

    private FakeFluidGridInteractor fluidGridInteractor;
    private StorageChannel<Rs2FluidStack> storageChannel;
    private FluidGridEventHandlerImpl sut;

    @BeforeEach
    void setUp() {
        fluidGridInteractor = new FakeFluidGridInteractor(BUCKET_AMOUNT);
        storageChannel = StorageChannelTypes.FLUID.create();
        sut = new FluidGridEventHandlerImpl(fluidGridInteractor, storageChannel, true);
        storageChannel.addSource(StorageDiskImpl.createFluidStorageDisk(10_000));
    }

    @Test
    void Test_inserting_from_cursor() {
        // Arrange
        fluidGridInteractor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));

        // Act
        sut.onInsertFromCursor();

        // Assert
        assertFluidStack(fluidGridInteractor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 2000 - BUCKET_AMOUNT));
        assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, BUCKET_AMOUNT));
        assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
    }

    @Test
    void Test_inserting_partially_when_storage_is_full() {
        // Arrange
        fluidGridInteractor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 2000));
        storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 9500, Action.EXECUTE);

        // Act
        sut.onInsertFromCursor();

        // Assert
        assertFluidStack(fluidGridInteractor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1500));
        assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
        assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isPresent();
    }

    @Test
    void Test_inserting_nothing_when_storage_is_full() {
        // Arrange
        storageChannel.insert(new Rs2FluidStack(FluidStubs.WATER), 10_000, Action.EXECUTE);

        fluidGridInteractor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 1));

        // Act
        sut.onInsertFromCursor();

        // Assert
        assertFluidStack(fluidGridInteractor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1));
        assertFluidStackListContents(storageChannel.getStacks(), new Rs2FluidStack(FluidStubs.WATER, 10_000));
        assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
    }

    @Test
    void Test_inserting_nothing_when_inactive() {
        // Arrange
        fluidGridInteractor.setCursorStack(new Rs2FluidStack(FluidStubs.WATER, 1));

        sut.onActiveChanged(false);

        // Act
        sut.onInsertFromCursor();

        // Assert
        assertFluidStack(fluidGridInteractor.getCursorStack(), new Rs2FluidStack(FluidStubs.WATER, 1));
        assertFluidStackListContents(storageChannel.getStacks());
        assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
    }

    @Test
    void Test_inserting_when_nothing_on_cursor() {
        // Act
        sut.onInsertFromCursor();

        // Assert
        assertThat(fluidGridInteractor.getCursorStack().isEmpty()).isTrue();
        assertFluidStackListContents(storageChannel.getStacks());
        assertThat(storageChannel.getTracker().getEntry(new Rs2FluidStack(FluidStubs.WATER))).isEmpty();
    }
}

package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Optional;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private final FluidGridInteractor fluidGridInteractor;
    private final StorageChannel<Rs2FluidStack> storageChannel;
    private boolean active;

    public FluidGridEventHandlerImpl(FluidGridInteractor fluidGridInteractor, StorageChannel<Rs2FluidStack> storageChannel, boolean active) {
        this.fluidGridInteractor = fluidGridInteractor;
        this.storageChannel = storageChannel;
        this.active = active;
    }

    @Override
    public void onInsertFromCursor() {
        if (!active) {
            return;
        }
        Rs2FluidStack stack = fluidGridInteractor.extractBucketFromCursor(Action.SIMULATE);
        if (!stack.isEmpty()) {
            Optional<Rs2FluidStack> remainder = storageChannel.insert(stack, stack.getAmount(), Action.SIMULATE);
            if (remainder.isEmpty() || remainder.get().getAmount() != stack.getAmount()) {
                long toExtract = remainder.isEmpty() ? stack.getAmount() : (stack.getAmount() - remainder.get().getAmount());
                stack = fluidGridInteractor.extractFromCursor(Action.EXECUTE, toExtract);
                storageChannel.insert(stack, stack.getAmount(), fluidGridInteractor);
            }
        }
    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
    }
}

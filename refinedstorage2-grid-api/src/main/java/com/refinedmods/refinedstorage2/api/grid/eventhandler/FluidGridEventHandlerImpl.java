package com.refinedmods.refinedstorage2.api.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Optional;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private final FluidGridInteractor interactor;
    private final StorageChannel<Rs2FluidStack> storageChannel;
    private boolean active;

    public FluidGridEventHandlerImpl(FluidGridInteractor interactor, StorageChannel<Rs2FluidStack> storageChannel, boolean active) {
        this.interactor = interactor;
        this.storageChannel = storageChannel;
        this.active = active;
    }

    @Override
    public void onInsertFromCursor() {
        if (!active) {
            return;
        }
        // TODO: Support SINGLE and ENTIRE_STACK
        Rs2FluidStack stack = interactor.extractBucketFromCursor(Action.SIMULATE);
        if (!stack.isEmpty()) {
            Optional<Rs2FluidStack> remainder = storageChannel.insert(stack, stack.getAmount(), Action.SIMULATE);
            if (remainder.isEmpty() || remainder.get().getAmount() != stack.getAmount()) {
                long toExtract = remainder.isEmpty() ? stack.getAmount() : (stack.getAmount() - remainder.get().getAmount());
                stack = interactor.extractFromCursor(Action.EXECUTE, toExtract);
                storageChannel.insert(stack, stack.getAmount(), interactor);
            }
        }
    }

    @Override
    public long onInsertFromTransfer(Rs2FluidStack stack) {
        long count = stack.getAmount();

        if (!active) {
            return count;
        }

        Rs2FluidStack remainderSimulated = storageChannel.insert(stack, count, Action.SIMULATE).orElse(Rs2FluidStack.EMPTY);

        if (remainderSimulated.isEmpty() || remainderSimulated.getAmount() != count) {
            return storageChannel
                    .insert(stack, count, interactor)
                    .map(Rs2FluidStack::getAmount)
                    .orElse(0L);
        }

        return count;
    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
    }
}

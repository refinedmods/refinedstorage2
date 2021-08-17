package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridEventHandlerImpl implements GridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(GridEventHandlerImpl.class);
    private final StorageChannel<Rs2ItemStack> storageChannel;
    private final GridInteractor interactor;
    private boolean active;

    public GridEventHandlerImpl(boolean active, StorageChannel<Rs2ItemStack> storageChannel, GridInteractor interactor) {
        this.active = active;
        this.storageChannel = storageChannel;
        this.interactor = interactor;
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        if (!active) {
            return;
        }

        Rs2ItemStack cursorStack = interactor.getCursorStack();
        LOGGER.info("Inserting {} from cursor with mode {}", cursorStack, mode);

        if (cursorStack.isEmpty()) {
            return;
        }

        if (mode == GridInsertMode.SINGLE) {
            Rs2ItemStack remainder = insertSingleItemFromCursor(cursorStack);
            interactor.setCursorStack(remainder);
        } else if (mode == GridInsertMode.ENTIRE_STACK) {
            Rs2ItemStack remainder = insertEntireStackFromCursor(cursorStack);
            interactor.setCursorStack(remainder);
        }
    }

    private Rs2ItemStack insertEntireStackFromCursor(Rs2ItemStack cursorStack) {
        Rs2ItemStack remainder;
        long count = cursorStack.getAmount();
        Rs2ItemStack remainderSimulated = storageChannel
                .insert(cursorStack, count, Action.SIMULATE)
                .orElse(Rs2ItemStack.EMPTY);

        if (remainderSimulated.isEmpty() || remainderSimulated.getAmount() != count) {
            remainder = storageChannel
                    .insert(cursorStack, count, interactor)
                    .orElse(Rs2ItemStack.EMPTY);
        } else {
            remainder = cursorStack;
        }
        return remainder;
    }

    private Rs2ItemStack insertSingleItemFromCursor(Rs2ItemStack cursorStack) {
        Rs2ItemStack remainder;
        if (!storageChannel.insert(cursorStack, 1, Action.SIMULATE).isPresent()) {
            storageChannel.insert(cursorStack, 1, interactor);
            cursorStack.decrement(1);
        }
        remainder = cursorStack;
        return remainder;
    }

    @Override
    public Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack) {
        if (!active) {
            return slotStack;
        }

        long count = slotStack.getAmount();

        Rs2ItemStack remainderSimulated = storageChannel.insert(slotStack, count, Action.SIMULATE).orElse(Rs2ItemStack.EMPTY);

        if (remainderSimulated.isEmpty() || remainderSimulated.getAmount() != count) {
            return storageChannel.insert(slotStack, count, interactor).orElse(Rs2ItemStack.EMPTY);
        }

        return slotStack;
    }

    @Override
    public void onExtract(Rs2ItemStack stack, GridExtractMode mode) {
        if ((mode.isCursorLike() && !interactor.getCursorStack().isEmpty()) || !active) {
            return;
        }

        long totalSize = getTotalSize(stack);
        if (totalSize == 0) {
            return;
        }

        long size = getSize(totalSize, mode);

        storageChannel.extract(stack, size, Action.SIMULATE)
                .map(extractedSimulated -> handleExtracted(mode, extractedSimulated, stack, size))
                .ifPresent(remainder -> {
                    if (!remainder.isEmpty()) {
                        storageChannel.insert(remainder, remainder.getAmount(), Action.EXECUTE);
                    }
                });
    }

    private long getTotalSize(Rs2ItemStack stack) {
        long totalSize = storageChannel.get(stack).map(Rs2ItemStack::getAmount).orElse(0L);

        if (totalSize > stack.getMaxCount()) {
            totalSize = stack.getMaxCount();
        }

        return totalSize;
    }

    private Rs2ItemStack handleExtracted(GridExtractMode mode, Rs2ItemStack extractedSimulated, Rs2ItemStack stack, long size) {
        return switch (mode) {
            case CURSOR_STACK, CURSOR_HALF -> handleExtractToCursor(stack, size);
            case PLAYER_INVENTORY_STACK -> handleExtractToPlayerInventory(extractedSimulated);
            default -> Rs2ItemStack.EMPTY;
        };
    }

    private Rs2ItemStack handleExtractToPlayerInventory(Rs2ItemStack extractedSimulated) {
        Rs2ItemStack remainderSimulated = interactor.insertIntoInventory(extractedSimulated, -1, Action.SIMULATE);
        if (remainderSimulated.isEmpty() || remainderSimulated.getAmount() != extractedSimulated.getAmount()) {
            return storageChannel
                    .extract(extractedSimulated, extractedSimulated.getAmount(), interactor)
                    .map(extracted -> interactor.insertIntoInventory(extracted, -1, Action.EXECUTE))
                    .orElse(Rs2ItemStack.EMPTY);
        }
        return Rs2ItemStack.EMPTY;
    }

    private Rs2ItemStack handleExtractToCursor(Rs2ItemStack stack, long size) {
        storageChannel.extract(stack, size, interactor).ifPresent(interactor::setCursorStack);
        return Rs2ItemStack.EMPTY;
    }

    private long getSize(long totalSize, GridExtractMode mode) {
        return switch (mode) {
            case CURSOR_STACK, PLAYER_INVENTORY_STACK -> totalSize;
            case CURSOR_HALF -> totalSize == 1 ? 1 : totalSize / 2;
            default -> 0;
        };
    }

    @Override
    public void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onScroll(Rs2ItemStack template, int slot, GridScrollMode mode) {
        if (!active) {
            return;
        }

        switch (mode) {
            case GRID_TO_INVENTORY:
                handleExtractFromGridToInventory(template, slot);
                break;
            case GRID_TO_CURSOR:
                handleExtractFromGridToCursor(template);
                break;
            case INVENTORY_TO_GRID:
                handleExtractFromInventoryToGrid(template, slot);
                break;
        }
    }

    private void handleExtractFromInventoryToGrid(Rs2ItemStack template, int slot) {
        Rs2ItemStack extractedSimulated = interactor.extractFromInventory(template, slot, 1, Action.SIMULATE);
        if (!extractedSimulated.isEmpty()) {
            Optional<Rs2ItemStack> remainderSimulated = storageChannel.insert(extractedSimulated, extractedSimulated.getAmount(), Action.SIMULATE);
            if (!remainderSimulated.isPresent() || remainderSimulated.get().getAmount() != extractedSimulated.getAmount()) {
                Rs2ItemStack extracted = interactor.extractFromInventory(template, slot, 1, Action.EXECUTE);
                if (!extracted.isEmpty()) {
                    storageChannel
                            .insert(extracted, extracted.getAmount(), interactor)
                            .ifPresent(remainder -> interactor.insertIntoInventory(remainder, -1, Action.EXECUTE));
                }
            }
        }
    }

    private void handleExtractFromGridToInventory(Rs2ItemStack template, int preferredSlot) {
        storageChannel.extract(template, 1, Action.SIMULATE).ifPresent(stack -> {
            Rs2ItemStack remainderSimulated = interactor.insertIntoInventory(stack, preferredSlot, Action.SIMULATE);
            if (remainderSimulated.isEmpty()) {
                storageChannel
                        .extract(template, 1, interactor)
                        .map(extracted -> interactor.insertIntoInventory(extracted, preferredSlot, Action.EXECUTE))
                        .ifPresent(remainder -> {
                            if (!remainder.isEmpty()) {
                                storageChannel.insert(remainder, remainder.getAmount(), Action.EXECUTE);
                            }
                        });
            }
        });
    }

    private void handleExtractFromGridToCursor(Rs2ItemStack template) {
        Rs2ItemStack cursorStack = interactor.getCursorStack();
        if (!cursorStack.isEmpty()) {
            if (!cursorStack.getItem().equals(template.getItem())) {
                return;
            }
            if (cursorStack.getAmount() + 1 > cursorStack.getMaxCount()) {
                return;
            }
        }

        storageChannel.extract(template, 1, Action.SIMULATE).ifPresent(extractedSimulated -> {
            storageChannel.extract(template, 1, interactor).ifPresent(extracted -> {
                if (cursorStack.isEmpty()) {
                    interactor.setCursorStack(extracted);
                } else {
                    cursorStack.increment(1);
                    interactor.setCursorStack(cursorStack);
                }
            });
        });
    }
}

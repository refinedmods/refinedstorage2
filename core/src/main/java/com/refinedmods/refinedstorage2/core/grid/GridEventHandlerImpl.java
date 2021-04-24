package com.refinedmods.refinedstorage2.core.grid;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridEventHandlerImpl implements GridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(GridEventHandlerImpl.class);

    private boolean active;
    private final StorageChannel<ItemStack> storageChannel;
    private final GridInteractor interactor;

    public GridEventHandlerImpl(boolean active, StorageChannel<ItemStack> storageChannel, GridInteractor interactor) {
        this.active = active;
        this.storageChannel = storageChannel;
        this.interactor = interactor;
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        if (!active) {
            return;
        }

        ItemStack cursorStack = interactor.getCursorStack();
        LOGGER.info("Inserting {} from cursor with mode {}", cursorStack, mode);

        if (cursorStack.isEmpty()) {
            return;
        }

        if (mode == GridInsertMode.SINGLE) {
            ItemStack remainder = insertSingleItemFromCursor(cursorStack);
            interactor.setCursorStack(remainder);
        } else if (mode == GridInsertMode.ENTIRE_STACK) {
            ItemStack remainder = insertEntireStackFromCursor(cursorStack);
            interactor.setCursorStack(remainder);
        }
    }

    private ItemStack insertEntireStackFromCursor(ItemStack cursorStack) {
        ItemStack remainder;
        int count = cursorStack.getCount();
        ItemStack remainderSimulated = storageChannel
            .insert(cursorStack, count, Action.SIMULATE)
            .orElse(ItemStack.EMPTY);

        if (remainderSimulated.isEmpty() || remainderSimulated.getCount() != count) {
            remainder = storageChannel
                .insert(cursorStack, count, interactor)
                .orElse(ItemStack.EMPTY);
        } else {
            remainder = cursorStack;
        }
        return remainder;
    }

    private ItemStack insertSingleItemFromCursor(ItemStack cursorStack) {
        ItemStack remainder;
        if (!storageChannel.insert(cursorStack, 1, Action.SIMULATE).isPresent()) {
            storageChannel.insert(cursorStack, 1, interactor);
            cursorStack.decrement(1);
        }
        remainder = cursorStack;
        return remainder;
    }

    @Override
    public ItemStack onInsertFromTransfer(ItemStack slotStack) {
        if (!active) {
            return slotStack;
        }

        int count = slotStack.getCount();

        ItemStack remainderSimulated = storageChannel.insert(slotStack, count, Action.SIMULATE).orElse(ItemStack.EMPTY);

        if (remainderSimulated.isEmpty() || remainderSimulated.getCount() != count) {
            return storageChannel.insert(slotStack, count, interactor).orElse(ItemStack.EMPTY);
        }

        return slotStack;
    }

    @Override
    public void onExtract(ItemStack stack, GridExtractMode mode) {
        if ((mode.isCursorLike() && !interactor.getCursorStack().isEmpty()) || !active) {
            return;
        }

        int totalSize = getTotalSize(stack);
        if (totalSize == 0) {
            return;
        }

        int size = getSize(totalSize, mode);

        storageChannel.extract(stack, size, Action.SIMULATE)
            .map(extractedSimulated -> handleExtracted(mode, extractedSimulated, stack, size))
            .ifPresent(remainder -> {
                if (!remainder.isEmpty()) {
                    storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
                }
            });
    }

    private int getTotalSize(ItemStack stack) {
        int totalSize = storageChannel.get(stack).map(ItemStack::getCount).orElse(0);

        if (totalSize > stack.getMaxCount()) {
            totalSize = stack.getMaxCount();
        }

        return totalSize;
    }

    private ItemStack handleExtracted(GridExtractMode mode, ItemStack extractedSimulated, ItemStack stack, int size) {
        switch (mode) {
            case CURSOR_STACK:
            case CURSOR_HALF:
                return handleExtractToCursor(stack, size);
            case PLAYER_INVENTORY_STACK:
                return handleExtractToPlayerInventory(extractedSimulated);
            default:
                return ItemStack.EMPTY;
        }
    }

    private ItemStack handleExtractToPlayerInventory(ItemStack extractedSimulated) {
        ItemStack remainderSimulated = interactor.insertIntoInventory(extractedSimulated, -1, Action.SIMULATE);
        if (remainderSimulated.isEmpty() || remainderSimulated.getCount() != extractedSimulated.getCount()) {
            return storageChannel
                .extract(extractedSimulated, extractedSimulated.getCount(), interactor)
                .map(extracted -> interactor.insertIntoInventory(extracted, -1, Action.EXECUTE))
                .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack handleExtractToCursor(ItemStack stack, int size) {
        storageChannel.extract(stack, size, interactor).ifPresent(interactor::setCursorStack);
        return ItemStack.EMPTY;
    }

    private int getSize(int totalSize, GridExtractMode mode) {
        switch (mode) {
            case CURSOR_STACK:
            case PLAYER_INVENTORY_STACK:
                return totalSize;
            case CURSOR_HALF:
                return totalSize == 1 ? 1 : totalSize / 2;
            default:
                return 0;
        }
    }

    @Override
    public void onItemUpdate(ItemStack template, int amount, StorageTracker.Entry trackerEntry) {

    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
    }

    @Override
    public void onScroll(ItemStack template, int slot, GridScrollMode mode) {
        if (!active) {
            return;
        }

        switch (mode) {
            case GRID_TO_INVENTORY_SINGLE_STACK:
            case GRID_TO_INVENTORY_STACK:
                handleExtractFromGrid(template, slot, mode);
                break;
            case INVENTORY_TO_GRID_SINGLE_STACK:
            case INVENTORY_TO_GRID_STACK:
                handleExtractFromInventory(template, slot, mode);
                break;
        }
    }

    private void handleExtractFromInventory(ItemStack template, int slot, GridScrollMode mode) {
        int size = mode == GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK ? 1 : template.getMaxCount();

        ItemStack extractedSimulated = interactor.extractFromInventory(template, slot, size, Action.SIMULATE);
        if (!extractedSimulated.isEmpty()) {
            Optional<ItemStack> remainderSimulated = storageChannel.insert(extractedSimulated, extractedSimulated.getCount(), Action.SIMULATE);
            if (!remainderSimulated.isPresent() || remainderSimulated.get().getCount() != extractedSimulated.getCount()) {
                ItemStack extracted = interactor.extractFromInventory(template, slot, size, Action.EXECUTE);
                if (!extracted.isEmpty()) {
                    storageChannel
                        .insert(extracted, extracted.getCount(), interactor)
                        .ifPresent(remainder -> interactor.insertIntoInventory(remainder, -1, Action.EXECUTE));
                }
            }
        }
    }

    private void handleExtractFromGrid(ItemStack template, int preferredSlot, GridScrollMode mode) {
        int size = mode == GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK ? 1 : template.getMaxCount();

        storageChannel.extract(template, size, Action.SIMULATE).ifPresent(stack -> {
            ItemStack remainderSimulated = interactor.insertIntoInventory(stack, preferredSlot, Action.SIMULATE);
            if (remainderSimulated.isEmpty() || remainderSimulated.getCount() != size) {
                storageChannel
                    .extract(template, size, interactor)
                    .map(extracted -> interactor.insertIntoInventory(extracted, preferredSlot, Action.EXECUTE))
                    .ifPresent(remainder -> {
                        if (!remainder.isEmpty()) {
                            storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
                        }
                    });
            }
        });
    }
}

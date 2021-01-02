package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridEventHandlerImpl implements GridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(GridEventHandlerImpl.class);

    private final StorageChannel<ItemStack> storageChannel;
    private final GridInteractor interactor;

    public GridEventHandlerImpl(StorageChannel<ItemStack> storageChannel, GridInteractor interactor) {
        this.storageChannel = storageChannel;
        this.interactor = interactor;
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        ItemStack cursorStack = interactor.getCursorStack();
        LOGGER.info("Inserting {} from cursor with mode {}", cursorStack, mode);

        if (cursorStack.isEmpty()) {
            return;
        }

        ItemStack remainder;
        if (mode == GridInsertMode.SINGLE) {
            if (!storageChannel.insert(cursorStack, 1, Action.SIMULATE).isPresent()) {
                storageChannel.getTracker().onChanged(cursorStack, interactor.getName());
                storageChannel.insert(cursorStack, 1, Action.EXECUTE);
                cursorStack.decrement(1);
            }
            remainder = cursorStack;
        } else {
            int count = cursorStack.getCount();
            remainder = storageChannel
                .insert(cursorStack, count, Action.EXECUTE)
                .orElse(ItemStack.EMPTY);

            if (remainder.isEmpty() || remainder.getCount() != count) {
                storageChannel.getTracker().onChanged(cursorStack, interactor.getName());
            }
        }

        interactor.setCursorStack(remainder);
    }

    @Override
    public void onInsertFromTransfer(Slot slot) {
        int count = slot.getStack().getCount();

        ItemStack remainder = storageChannel.insert(slot.getStack(), count, Action.EXECUTE).orElse(ItemStack.EMPTY);

        if (remainder.isEmpty() || remainder.getCount() != count) {
            storageChannel.getTracker().onChanged(slot.getStack(), interactor.getName());
        }

        slot.setStack(remainder);
    }

    @Override
    public void onExtract(ItemStack stack, GridExtractMode mode) {
        int totalSize = storageChannel.get(stack).map(ItemStack::getCount).orElse(0);
        if (totalSize == 0) {
            return;
        }

        if (totalSize > stack.getMaxCount()) {
            totalSize = stack.getMaxCount();
        }

        if (mode.isCursorLike() && !interactor.getCursorStack().isEmpty()) {
            return;
        }

        int size = getSize(totalSize, mode);

        storageChannel.extract(stack, size, Action.EXECUTE)
            .map(extracted -> handleExtracted(mode, extracted))
            .ifPresent(remainder -> {
                if (!remainder.isEmpty()) {
                    storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
                }
            });
    }

    private ItemStack handleExtracted(GridExtractMode mode, ItemStack extracted) {
        switch (mode) {
            case CURSOR_STACK:
            case CURSOR_HALF:
                interactor.setCursorStack(extracted);
                storageChannel.getTracker().onChanged(extracted, interactor.getName());
                return ItemStack.EMPTY;
            case PLAYER_INVENTORY_STACK:
                ItemStack remainder = interactor.insertIntoInventory(extracted, -1);
                if (remainder.isEmpty() || remainder.getCount() != extracted.getCount()) {
                    storageChannel.getTracker().onChanged(extracted, interactor.getName());
                }
                return remainder;
            default:
                return ItemStack.EMPTY;
        }
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
    public void onItemUpdate(ItemStack template, int amount) {

    }

    @Override
    public void onScroll(ItemStack template, int slot, GridScrollMode mode) {
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

        ItemStack result = interactor.extractFromInventory(template, slot, size);
        if (!result.isEmpty()) {
            storageChannel.insert(result, result.getCount(), Action.EXECUTE)
                .ifPresent(remainder -> interactor.insertIntoInventory(remainder, -1));
        }
    }

    private void handleExtractFromGrid(ItemStack template, int preferredSlot, GridScrollMode mode) {
        int size = mode == GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK ? 1 : template.getMaxCount();

        storageChannel.extract(template, size, Action.EXECUTE).ifPresent(stack -> {
            ItemStack remainder = interactor.insertIntoInventory(stack, preferredSlot);
            if (!remainder.isEmpty()) {
                storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
            }
        });
    }
}

package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
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
                storageChannel.insert(cursorStack, 1, Action.EXECUTE);
                cursorStack.decrement(1);
            }
            remainder = cursorStack;
        } else {
            remainder = storageChannel
                .insert(cursorStack, cursorStack.getCount(), Action.EXECUTE)
                .orElse(ItemStack.EMPTY);
        }

        interactor.setCursorStack(remainder);
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
                return ItemStack.EMPTY;
            case PLAYER_INVENTORY_STACK:
                return interactor.insertIntoInventory(extracted);
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
    public void onScroll(ItemStack template, GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY_SINGLE_STACK:
            case GRID_TO_INVENTORY_STACK:
                handleExtractFromGrid(template, mode);
                break;
            case INVENTORY_TO_GRID_SINGLE_STACK:
            case INVENTORY_TO_GRID_STACK:
                handleExtractFromInventory(template, mode);
                break;
        }
    }

    private void handleExtractFromInventory(ItemStack template, GridScrollMode mode) {
        int size = mode == GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK ? 1 : template.getMaxCount();

        ItemStack result = interactor.extractFromInventory(template, size);
        if (!result.isEmpty()) {
            storageChannel.insert(result, result.getCount(), Action.EXECUTE)
                .ifPresent(interactor::insertIntoInventory);
        }
    }

    private void handleExtractFromGrid(ItemStack template, GridScrollMode mode) {
        int size = mode == GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK ? 1 : template.getMaxCount();

        storageChannel.extract(template, size, Action.EXECUTE).ifPresent(stack -> {
            ItemStack remainder = interactor.insertIntoInventory(stack);
            if (!remainder.isEmpty()) {
                storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
            }
        });
    }
}

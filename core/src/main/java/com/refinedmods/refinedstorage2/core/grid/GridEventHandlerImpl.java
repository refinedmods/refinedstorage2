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

        storageChannel.extract(stack, size, Action.EXECUTE).ifPresent(extracted -> handleExtracted(mode, extracted));
    }

    private void handleExtracted(GridExtractMode mode, ItemStack extracted) {
        switch (mode) {
            case CURSOR_STACK:
                interactor.setCursorStack(extracted);
                break;
            case CURSOR_HALF:
                interactor.setCursorStack(extracted);
                break;
            case PLAYER_INVENTORY_STACK:
                interactor.insertIntoInventory(extracted);
                break;
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
}

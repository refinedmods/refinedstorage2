package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridScrollMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.FabricItemGridStack;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.ItemGridScreenHandler;

import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class ItemGridScreen extends GridScreen<Rs2ItemStack, ItemGridScreenHandler> {
    private final ItemGridEventHandler eventHandler;

    public ItemGridScreen(ItemGridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.eventHandler = handler;
    }

    @Override
    protected void renderStack(MatrixStack matrices, int slotX, int slotY, GridStack<Rs2ItemStack> stack) {
        itemRenderer.renderInGuiWithOverrides(((FabricItemGridStack) stack).getPlatformStack(), slotX, slotY);
    }

    @Override
    protected String getAmount(GridStack<Rs2ItemStack> stack) {
        return stack.isZeroed() ? "0" : String.valueOf(stack.getAmount());
    }

    @Override
    protected List<Text> getTooltip(GridStack<Rs2ItemStack> stack) {
        return getTooltipFromItem(((FabricItemGridStack) stack).getPlatformStack());
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        eventHandler.onInsertFromCursor(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridStack<Rs2ItemStack> stack) {
        eventHandler.onExtract(stack.getResourceAmount(), getExtractMode(clickedButton));
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, Rs2ItemStack stack, int slotIndex) {
        GridScrollMode mode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (mode == null) {
            return;
        }
        eventHandler.onScroll(stack, slotIndex, mode);
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridStack<Rs2ItemStack> stack) {
        int slotIndex = getScreenHandler().getPlayerInventorySlotThatHasStack(Rs2PlatformApiFacade.INSTANCE.itemStackConversion().toPlatform(stack.getResourceAmount()));
        GridScrollMode mode = getScrollModeWhenScrollingOnGridArea(up);
        if (mode == null) {
            return;
        }
        eventHandler.onScroll(stack.getResourceAmount(), slotIndex, mode);
    }

    private static GridExtractMode getExtractMode(int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.CURSOR_HALF;
        }
        if (hasShiftDown()) {
            return GridExtractMode.PLAYER_INVENTORY_STACK;
        }
        return GridExtractMode.CURSOR_STACK;
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE : GridInsertMode.ENTIRE_STACK;
    }

    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(boolean up) {
        if (hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(boolean up) {
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();
        if (shift && ctrl) {
            return null;
        }

        if (up) {
            if (shift) {
                return GridScrollMode.INVENTORY_TO_GRID;
            }
        } else {
            if (shift) {
                return GridScrollMode.GRID_TO_INVENTORY;
            } else if (ctrl) {
                return GridScrollMode.GRID_TO_CURSOR;
            }
        }

        return null;
    }
}

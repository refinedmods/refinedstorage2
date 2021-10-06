package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.ItemGridScreenHandler;

import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemGridScreen extends GridScreen<ItemResource, ItemGridScreenHandler> {
    public ItemGridScreen(ItemGridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderStack(MatrixStack matrices, int slotX, int slotY, GridResource<ItemResource> stack) {
        itemRenderer.renderInGuiWithOverrides(stack.getResourceAmount().getResource().getItemStack(), slotX, slotY);
    }

    @Override
    protected String getAmount(GridResource<ItemResource> stack) {
        return stack.isZeroed() ? "0" : String.valueOf(stack.getResourceAmount().getAmount());
    }

    @Override
    protected List<Text> getTooltip(GridResource<ItemResource> stack) {
        return getTooltipFromItem(stack.getResourceAmount().getResource().getItemStack());
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        getScreenHandler().insert(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridResource<ItemResource> stack) {
        getScreenHandler().onExtract(stack.getResourceAmount().getResource(), getExtractMode(clickedButton), shouldExtractToCursor());
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex) {
        // todo
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridResource<ItemResource> stack) {
        // todo
    }

    private static GridExtractMode getExtractMode(int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.HALF_RESOURCE;
        }
        return GridExtractMode.ENTIRE_RESOURCE;
    }

    private static boolean shouldExtractToCursor() {
        return !hasShiftDown();
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
    }

    /*
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
    }*/
}

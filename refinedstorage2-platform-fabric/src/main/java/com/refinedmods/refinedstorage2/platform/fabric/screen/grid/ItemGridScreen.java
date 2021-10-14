package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.ItemGridResource;
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
    protected void renderResource(MatrixStack matrices, int slotX, int slotY, GridResource<ItemResource> resource) {
        itemRenderer.renderInGuiWithOverrides(((ItemGridResource) resource).getItemStack(), slotX, slotY);
    }

    @Override
    protected String getAmount(GridResource<ItemResource> resource) {
        return resource.isZeroed() ? "0" : String.valueOf(resource.getResourceAmount().getAmount());
    }

    @Override
    protected List<Text> getTooltip(GridResource<ItemResource> resource) {
        return getTooltipFromItem(((ItemGridResource) resource).getItemStack());
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        getScreenHandler().onInsert(getInsertMode(clickedButton));
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridResource<ItemResource> resource) {
        getScreenHandler().onExtract(resource.getResourceAmount().getResource(), getExtractMode(clickedButton), shouldExtractToCursor());
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

    @Override
    protected void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex) {
        GridScrollMode scrollMode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (scrollMode == null) {
            return;
        }
        getScreenHandler().onScroll(new ItemResource(stack), scrollMode, slotIndex);
    }

    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(boolean up) {
        if (hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridResource<ItemResource> resource) {
        GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        getScreenHandler().onScroll(resource.getResourceAmount().getResource(), scrollMode, -1);
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

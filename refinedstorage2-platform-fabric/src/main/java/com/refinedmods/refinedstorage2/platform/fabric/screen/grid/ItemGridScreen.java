package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.ItemGridResource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ItemGridScreen extends GridScreen<ItemResource, ItemGridContainerMenu> {
    public ItemGridScreen(ItemGridContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
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

    @Override
    protected void renderResource(PoseStack poseStack, int slotX, int slotY, GridResource<ItemResource> resource) {
        itemRenderer.renderAndDecorateItem(((ItemGridResource) resource).getItemStack(), slotX, slotY);
    }

    @Override
    protected String getAmount(GridResource<ItemResource> resource) {
        if (resource.isZeroed()) {
            return "0";
        }
        return QuantityFormatter.formatWithUnits(resource.getResourceAmount().getAmount());
    }

    @Override
    protected List<Component> getTooltip(GridResource<ItemResource> resource) {
        return getTooltipFromItem(((ItemGridResource) resource).getItemStack());
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        getMenu().onInsert(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridResource<ItemResource> resource) {
        getMenu().onExtract(resource.getResourceAmount().getResource(), getExtractMode(clickedButton), shouldExtractToCursor());
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex) {
        GridScrollMode scrollMode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(new ItemResource(stack), scrollMode, slotIndex);
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridResource<ItemResource> resource) {
        GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(resource.getResourceAmount().getResource(), scrollMode, -1);
    }
}

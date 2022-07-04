package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ItemGridScreen extends AbstractGridScreen<ItemResource, ItemGridContainerMenu> {
    public ItemGridScreen(final ItemGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }

    private static GridInsertMode getInsertMode(final int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
    }

    private static GridExtractMode getExtractMode(final int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.HALF_RESOURCE;
        }
        return GridExtractMode.ENTIRE_RESOURCE;
    }

    private static boolean shouldExtractToCursor() {
        return !hasShiftDown();
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(final boolean up) {
        if (hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(final boolean up) {
        final boolean shift = hasShiftDown();
        final boolean ctrl = hasControlDown();
        if (shift && ctrl) {
            return null;
        }
        return getScrollModeWhenScorllingOnGridArea(up, shift, ctrl);
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScorllingOnGridArea(final boolean up,
                                                                       final boolean shift,
                                                                       final boolean ctrl) {
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
    protected void renderResource(final PoseStack poseStack,
                                  final int slotX,
                                  final int slotY,
                                  final AbstractGridResource<ItemResource> resource) {
        itemRenderer.renderAndDecorateItem(((ItemGridResource) resource).getItemStack(), slotX, slotY);
    }

    @Override
    protected String getAmount(final AbstractGridResource<ItemResource> resource) {
        if (resource.isZeroed()) {
            return "0";
        }
        return QuantityFormatter.formatWithUnits(resource.getResourceAmount().getAmount());
    }

    @Override
    protected String getAmountInTooltip(final AbstractGridResource<ItemResource> resource) {
        if (resource.isZeroed()) {
            return "0";
        }
        return QuantityFormatter.format(resource.getResourceAmount().getAmount());
    }

    @Override
    protected List<Component> getTooltip(final AbstractGridResource<ItemResource> resource) {
        return getTooltipFromItem(((ItemGridResource) resource).getItemStack());
    }

    @Override
    protected void mouseClickedInGrid(final int clickedButton) {
        getMenu().onInsert(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(final int clickedButton, final AbstractGridResource<ItemResource> resource) {
        getMenu().onExtract(
            resource.getResourceAmount().getResource(),
            getExtractMode(clickedButton),
            shouldExtractToCursor()
        );
    }

    @Override
    protected void mouseScrolledInInventory(final boolean up, final ItemStack stack, final int slotIndex) {
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(new ItemResource(stack.getItem(), stack.getTag()), scrollMode, slotIndex);
    }

    @Override
    protected void mouseScrolledInGrid(final boolean up, final AbstractGridResource<ItemResource> resource) {
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(resource.getResourceAmount().getResource(), scrollMode, -1);
    }
}

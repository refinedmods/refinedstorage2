package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FluidGridScreen extends AbstractGridScreen<FluidResource, FluidGridContainerMenu> {
    public FluidGridScreen(final FluidGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
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

    @Override
    protected void mouseClickedInGrid(final int clickedButton, final AbstractGridResource resource) {
        getMenu().onExtract(
            getFluidResource(resource),
            getExtractMode(clickedButton),
            shouldExtractToCursor()
        );
    }

    private FluidResource getFluidResource(final AbstractGridResource resource) {
        return (FluidResource) resource.getResourceAmount().getResource();
    }

    @Override
    protected void mouseScrolledInInventory(final boolean up, final ItemStack stack, final int slotIndex) {
        // no op
    }

    @Override
    protected void mouseScrolledInGrid(final boolean up, final AbstractGridResource resource) {
        // no op
    }
}

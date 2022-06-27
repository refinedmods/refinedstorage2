package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FluidGridScreen extends GridScreen<FluidResource, FluidGridContainerMenu> {
    public FluidGridScreen(final FluidGridContainerMenu menu, final Inventory inventory, final Component title) {
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

    @Override
    protected void renderResource(final PoseStack poseStack, final int slotX, final int slotY, final GridResource<FluidResource> resource) {
        Platform.INSTANCE.getFluidRenderer().render(poseStack, slotX, slotY, getBlitOffset(), resource.getResourceAmount().getResource());
    }

    @Override
    protected String getAmount(final GridResource<FluidResource> resource) {
        if (resource.isZeroed()) {
            return "0";
        }
        return Platform.INSTANCE.getBucketQuantityFormatter().formatWithUnits(resource.getResourceAmount().getAmount());
    }

    @Override
    protected String getAmountInTooltip(final GridResource<FluidResource> resource) {
        if (resource.isZeroed()) {
            return "0";
        }
        return Platform.INSTANCE.getBucketQuantityFormatter().format(resource.getResourceAmount().getAmount());
    }

    @Override
    protected List<Component> getTooltip(final GridResource<FluidResource> resource) {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(resource.getResourceAmount().getResource());
    }

    @Override
    protected void mouseClickedInGrid(final int clickedButton) {
        getMenu().onInsert(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(final int clickedButton, final GridResource<FluidResource> resource) {
        getMenu().onExtract(resource.getResourceAmount().getResource(), getExtractMode(clickedButton), shouldExtractToCursor());
    }

    @Override
    protected void mouseScrolledInInventory(final boolean up, final ItemStack stack, final int slotIndex) {
        // no op
    }

    @Override
    protected void mouseScrolledInGrid(final boolean up, final GridResource<FluidResource> resource) {
        // no op
    }
}

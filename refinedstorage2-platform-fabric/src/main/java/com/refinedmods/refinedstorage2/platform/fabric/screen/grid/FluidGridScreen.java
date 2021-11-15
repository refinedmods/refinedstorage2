package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FluidGridResource;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FluidGridScreen extends GridScreen<FluidResource, FluidGridContainerMenu> {
    public FluidGridScreen(FluidGridContainerMenu menu, Inventory inventory, Component title) {
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

    @Override
    protected void renderResource(PoseStack poseStack, int slotX, int slotY, GridResource<FluidResource> resource) {
        FluidVariant variant = ((FluidGridResource) resource).getFluidVariant();
        ScreenUtil.renderFluid(poseStack, slotX, slotY, getBlitOffset(), variant);
    }

    @Override
    protected String getAmount(GridResource<FluidResource> resource) {
        return FabricQuantityFormatter.formatDropletsAsBucket(resource.isZeroed() ? 0 : resource.getResourceAmount().getAmount());
    }

    @Override
    protected List<Component> getTooltip(GridResource<FluidResource> resource) {
        return FluidVariantRendering.getTooltip(
                ((FluidGridResource) resource).getFluidVariant(),
                minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
        );
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        getMenu().onInsert(getInsertMode(clickedButton));
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridResource<FluidResource> resource) {
        getMenu().onExtract(resource.getResourceAmount().getResource(), getExtractMode(clickedButton), shouldExtractToCursor());
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex) {
        // no op
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridResource<FluidResource> resource) {
        // no op
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FluidGridResource;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(variant);
        if (sprite != null) {
            renderFluidSprite(poseStack, slotX, slotY, variant, sprite);
        }
    }

    @Override
    protected String getAmount(GridResource<FluidResource> resource) {
        return FabricQuantityFormatter.formatDropletsAsBucket(resource.isZeroed() ? 0 : resource.getResourceAmount().getAmount());
    }

    private void renderFluidSprite(PoseStack poseStack, int slotX, int slotY, FluidVariant variant, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlas().location());

        int packedRgb = FluidVariantRendering.getColor(variant);
        int r = (packedRgb >> 16 & 255);
        int g = (packedRgb >> 8 & 255);
        int b = (packedRgb & 255);

        int slotXEnd = slotX + 16;
        int slotYEnd = slotY + 16;
        int z = getBlitOffset();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder
                .vertex(poseStack.last().pose(), (float) slotX, (float) slotYEnd, (float) z)
                .uv(sprite.getU0(), sprite.getV1())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), (float) slotXEnd, (float) slotYEnd, (float) z)
                .uv(sprite.getU1(), sprite.getV1())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), (float) slotXEnd, (float) slotY, (float) z)
                .uv(sprite.getU1(), sprite.getV0())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), (float) slotX, (float) slotY, (float) z)
                .uv(sprite.getU0(), sprite.getV0())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
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

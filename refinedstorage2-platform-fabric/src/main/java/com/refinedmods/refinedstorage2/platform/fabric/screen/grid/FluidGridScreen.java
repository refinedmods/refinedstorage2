package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FluidGridScreen extends GridScreen<FluidResource, FluidGridScreenHandler> {
    public FluidGridScreen(FluidGridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderStack(MatrixStack matrices, int slotX, int slotY, GridResource<FluidResource> stack) {
        FluidVariant variant = stack.getResourceAmount().getResource().getFluidVariant();
        Sprite sprite = FluidVariantRendering.getSprite(variant);
        if (sprite != null) {
            renderFluidSprite(matrices, slotX, slotY, variant, sprite);
        }
    }

    @Override
    protected String getAmount(GridResource<FluidResource> stack) {
        return FabricQuantityFormatter.formatDropletsAsBucket(stack.isZeroed() ? 0 : stack.getResourceAmount().getAmount());
    }

    private void renderFluidSprite(MatrixStack matrices, int slotX, int slotY, FluidVariant variant, Sprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());

        int packedRgb = FluidVariantRendering.getColor(variant);
        int r = (packedRgb >> 16 & 255);
        int g = (packedRgb >> 8 & 255);
        int b = (packedRgb & 255);

        int slotXEnd = slotX + 16;
        int slotYEnd = slotY + 16;
        int z = getZOffset();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder
                .vertex(matrices.peek().getModel(), (float) slotX, (float) slotYEnd, (float) z)
                .texture(sprite.getMinU(), sprite.getMaxV())
                .color(r, g, b, 255)
                .next();
        bufferBuilder
                .vertex(matrices.peek().getModel(), (float) slotXEnd, (float) slotYEnd, (float) z)
                .texture(sprite.getMaxU(), sprite.getMaxV())
                .color(r, g, b, 255)
                .next();
        bufferBuilder
                .vertex(matrices.peek().getModel(), (float) slotXEnd, (float) slotY, (float) z)
                .texture(sprite.getMaxU(), sprite.getMinV())
                .color(r, g, b, 255)
                .next();
        bufferBuilder
                .vertex(matrices.peek().getModel(), (float) slotX, (float) slotY, (float) z)
                .texture(sprite.getMinU(), sprite.getMinV())
                .color(r, g, b, 255)
                .next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    @Override
    protected List<Text> getTooltip(GridResource<FluidResource> stack) {
        return FluidVariantRendering.getTooltip(
                stack.getResourceAmount().getResource().getFluidVariant(),
                client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL
        );
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        // todo
    }

    private static GridInsertMode getInsertMode(int clickedButton) {
        return clickedButton == 1 ? GridInsertMode.SINGLE_RESOURCE : GridInsertMode.ENTIRE_RESOURCE;
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridResource<FluidResource> stack) {
        // todo
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, ItemStack stack, int slotIndex) {
        // todo
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridResource<FluidResource> stack) {
        // todo
    }
}

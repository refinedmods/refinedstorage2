package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.FabricFluidGridStack;
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
import net.minecraft.text.Text;

public class FluidGridScreen extends GridScreen<Rs2FluidStack, FluidGridScreenHandler> {
    private final FluidGridEventHandler eventHandler;

    public FluidGridScreen(FluidGridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.eventHandler = handler;
    }

    @Override
    protected void renderStack(MatrixStack matrices, int slotX, int slotY, GridStack<Rs2FluidStack> stack) {
        FluidVariant variant = ((FabricFluidGridStack) stack).getMcStack();
        Sprite sprite = FluidVariantRendering.getSprite(variant);
        if (sprite != null) {
            renderFluidSprite(matrices, slotX, slotY, variant, sprite);
        }
    }

    @Override
    protected String getAmount(GridStack<Rs2FluidStack> stack) {
        return FabricQuantityFormatter.formatDropletsAsBucket(stack.isZeroed() ? 0 : stack.getAmount());
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
    protected List<Text> getTooltip(GridStack<Rs2FluidStack> stack) {
        FluidVariant variant = ((FabricFluidGridStack) stack).getMcStack();
        return FluidVariantRendering.getTooltip(variant, client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton) {
        eventHandler.onInsertFromCursor();
    }

    @Override
    protected void mouseClickedInGrid(int clickedButton, GridStack<Rs2FluidStack> stack) {
        // no op
    }

    @Override
    protected void mouseScrolledInInventory(boolean up, Rs2ItemStack stack, int slotIndex) {
        // no op
    }

    @Override
    protected void mouseScrolledInGrid(boolean up, GridStack<Rs2FluidStack> stack) {
        // no op
    }
}

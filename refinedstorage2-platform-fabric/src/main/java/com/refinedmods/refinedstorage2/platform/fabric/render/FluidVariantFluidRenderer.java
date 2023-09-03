package com.refinedmods.refinedstorage2.platform.fabric.render;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.render.model.AbstractFluidRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public class FluidVariantFluidRenderer extends AbstractFluidRenderer {
    private final Map<FluidResource, FluidVariant> variantCache = new HashMap<>();

    private FluidVariant getFluidVariantFromCache(final FluidResource fluidResource) {
        return variantCache.computeIfAbsent(fluidResource, VariantUtil::toFluidVariant);
    }

    @Override
    public void render(final PoseStack poseStack,
                       final int x,
                       final int y,
                       final FluidResource fluidResource) {
        final FluidVariant fluidVariant = getFluidVariantFromCache(fluidResource);
        final TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite == null) {
            return;
        }
        final int packedRgb = FluidVariantRendering.getColor(fluidVariant);
        render(poseStack, x, y, packedRgb, sprite);
    }

    @Override
    public void render(final PoseStack poseStack,
                       final MultiBufferSource renderTypeBuffer,
                       final int light,
                       final FluidResource fluidResource) {
        final FluidVariant fluidVariant = getFluidVariantFromCache(fluidResource);
        final TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite == null) {
            return;
        }
        final int packedRgb = FluidVariantRendering.getColor(fluidVariant);
        render(poseStack, renderTypeBuffer, light, packedRgb, sprite);
    }

    @Override
    public List<Component> getTooltip(final FluidResource fluidResource) {
        return FluidVariantRendering.getTooltip(
            getFluidVariantFromCache(fluidResource),
            Minecraft.getInstance().options.advancedItemTooltips
                ? TooltipFlag.ADVANCED
                : TooltipFlag.NORMAL
        );
    }

    @Override
    public Component getDisplayName(final FluidResource fluidResource) {
        return FluidVariantAttributes.getName(getFluidVariantFromCache(fluidResource));
    }
}

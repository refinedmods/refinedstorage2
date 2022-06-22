package com.refinedmods.refinedstorage2.platform.fabric.render;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.render.model.FluidRendererImpl;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public class FluidVariantFluidRenderer extends FluidRendererImpl {
    private final Map<FluidResource, FluidVariant> variantCache = new HashMap<>();

    private FluidVariant getFluidVariantFromCache(FluidResource fluidResource) {
        return variantCache.computeIfAbsent(fluidResource, VariantUtil::toFluidVariant);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int z, FluidResource fluidResource) {
        FluidVariant fluidVariant = getFluidVariantFromCache(fluidResource);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite != null) {
            int packedRgb = FluidVariantRendering.getColor(fluidVariant);
            render(poseStack, x, y, z, packedRgb, sprite);
        }
    }

    @Override
    public List<Component> getTooltip(FluidResource fluidResource) {
        return FluidVariantRendering.getTooltip(
                getFluidVariantFromCache(fluidResource),
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
        );
    }
}

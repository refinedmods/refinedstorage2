package com.refinedmods.refinedstorage2.platform.forge.support.render;

import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.support.render.AbstractFluidRenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public class FluidStackFluidRenderer extends AbstractFluidRenderer {
    private final Map<FluidResource, FluidStack> stackCache = new HashMap<>();

    private FluidStack getFluidStackFromCache(final FluidResource fluidResource) {
        return stackCache.computeIfAbsent(
            fluidResource,
            r -> new FluidStack(r.fluid(), FluidType.BUCKET_VOLUME, r.tag())
        );
    }

    @Override
    public void render(final PoseStack poseStack, final int x, final int y, final FluidResource fluidResource) {
        final FluidStack stack = getFluidStackFromCache(fluidResource);
        final Fluid fluid = fluidResource.fluid();

        final IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);

        final int packedRgb = renderProperties.getTintColor(stack);
        final TextureAtlasSprite sprite = getStillFluidSprite(renderProperties, stack);

        render(poseStack, x, y, packedRgb, sprite);
    }

    @Override
    public void render(final PoseStack poseStack,
                       final MultiBufferSource renderTypeBuffer,
                       final int light,
                       final FluidResource fluidResource) {
        final FluidStack stack = getFluidStackFromCache(fluidResource);
        final Fluid fluid = fluidResource.fluid();

        final IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);

        final int packedRgb = renderProperties.getTintColor(stack);
        final TextureAtlasSprite sprite = getStillFluidSprite(renderProperties, stack);

        render(poseStack, renderTypeBuffer, light, packedRgb, sprite);
    }

    @Override
    public Component getDisplayName(final FluidResource fluidResource) {
        return fluidResource.fluid().getFluidType().getDescription();
    }

    private TextureAtlasSprite getStillFluidSprite(final IClientFluidTypeExtensions renderProperties,
                                                   final FluidStack fluidStack) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    @Override
    public List<Component> getTooltip(final FluidResource fluidResource) {
        return Collections.singletonList(getFluidStackFromCache(fluidResource).getDisplayName());
    }
}

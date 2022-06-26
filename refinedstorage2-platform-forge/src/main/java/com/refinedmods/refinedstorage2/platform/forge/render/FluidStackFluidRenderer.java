package com.refinedmods.refinedstorage2.platform.forge.render;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.render.model.FluidRendererImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class FluidStackFluidRenderer extends FluidRendererImpl {
    private final Map<FluidResource, FluidStack> stackCache = new HashMap<>();

    @NotNull
    private FluidStack getFluidStackFromCache(FluidResource fluidResource) {
        return stackCache.computeIfAbsent(fluidResource, r -> new FluidStack(r.fluid(), FluidType.BUCKET_VOLUME, r.tag()));
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int z, FluidResource fluidResource) {
        FluidStack stack = getFluidStackFromCache(fluidResource);
        Fluid fluid = fluidResource.fluid();

        IFluidTypeRenderProperties renderProperties = RenderProperties.get(fluid);

        int packedRgb = renderProperties.getColorTint(stack);
        TextureAtlasSprite sprite = getStillFluidSprite(renderProperties, stack);

        render(poseStack, x, y, z, packedRgb, sprite);
    }

    private TextureAtlasSprite getStillFluidSprite(IFluidTypeRenderProperties renderProperties, FluidStack fluidStack) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    @Override
    public List<Component> getTooltip(FluidResource fluidResource) {
        return Collections.singletonList(getFluidStackFromCache(fluidResource).getDisplayName());
    }
}

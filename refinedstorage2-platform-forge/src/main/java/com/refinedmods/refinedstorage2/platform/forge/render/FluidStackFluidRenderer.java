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
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidStackFluidRenderer extends FluidRendererImpl {
    private final Map<FluidResource, FluidStack> stackCache = new HashMap<>();

    @NotNull
    private FluidStack getFluidStackFromCache(FluidResource fluidResource) {
        return stackCache.computeIfAbsent(fluidResource, r -> new FluidStack(r.getFluid(), FluidAttributes.BUCKET_VOLUME, r.getTag()));
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int z, FluidResource fluidResource) {
        FluidAttributes attributes = fluidResource.getFluid().getAttributes();
        FluidStack stack = getFluidStackFromCache(fluidResource);
        int packedRgb = attributes.getColor(stack);
        TextureAtlasSprite sprite = getStillFluidSprite(attributes, stack);
        render(poseStack, x, y, z, packedRgb, sprite);
    }

    private TextureAtlasSprite getStillFluidSprite(FluidAttributes attributes, FluidStack fluidStack) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation fluidStill = attributes.getStillTexture(fluidStack);
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    @Override
    public List<Component> getTooltip(FluidResource fluidResource) {
        return Collections.singletonList(getFluidStackFromCache(fluidResource).getDisplayName());
    }
}

package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.TooltipFlag;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class FluidGridResourceFactory implements Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> {
    @Override
    public GridResource<FluidResource> apply(ResourceAmount<FluidResource> resourceAmount) {
        FluidVariant fluidVariant = toFluidVariant(resourceAmount.getResource());

        String name = getName(fluidVariant);
        String modId = getModId(fluidVariant);
        String modName = getModName(modId);

        Set<String> tags = getTags(fluidVariant);
        String tooltip = getTooltip(fluidVariant);

        return new FluidGridResource(resourceAmount, name, modId, modName, tags, tooltip);
    }

    private String getTooltip(FluidVariant fluidVariant) {
        return FluidVariantRendering
                .getTooltip(fluidVariant, TooltipFlag.Default.ADVANCED)
                .stream()
                .map(Component::getContents)
                .collect(Collectors.joining("\n"));
    }

    private Set<String> getTags(FluidVariant fluidVariant) {
        return FluidTags
                .getAllTags()
                .getMatchingTags(fluidVariant.getFluid())
                .stream()
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());
    }

    private String getModName(String modId) {
        return FabricLoader
                .getInstance()
                .getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName)
                .orElse("");
    }

    private String getModId(FluidVariant fluidVariant) {
        return Registry.FLUID.getKey(fluidVariant.getFluid()).getNamespace();
    }

    private String getName(FluidVariant fluidVariant) {
        return FluidVariantRendering.getName(fluidVariant).getString();
    }
}

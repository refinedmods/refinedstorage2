package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidGridResourceFactory implements Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> {
    @Override
    public GridResource<FluidResource> apply(ResourceAmount<FluidResource> resourceAmount) {
        FluidVariant fluidVariant = resourceAmount.getResource().toFluidVariant();

        String name = getName(fluidVariant);
        String modId = getModId(fluidVariant);
        String modName = getModName(modId);

        Set<String> tags = getTags(fluidVariant);
        String tooltip = getTooltip(fluidVariant);

        return new FluidGridResource(resourceAmount, name, modId, modName, tags, tooltip);
    }

    private String getTooltip(FluidVariant fluidVariant) {
        return FluidVariantRendering
                .getTooltip(fluidVariant, TooltipContext.Default.ADVANCED)
                .stream()
                .map(Text::asString)
                .collect(Collectors.joining("\n"));
    }

    private Set<String> getTags(FluidVariant fluidVariant) {
        return FluidTags
                .getTagGroup()
                .getTagsFor(fluidVariant.getFluid())
                .stream()
                .map(Identifier::getPath)
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
        return Registry.FLUID.getId(fluidVariant.getFluid()).getNamespace();
    }

    private String getName(FluidVariant fluidVariant) {
        return FluidVariantRendering.getName(fluidVariant).getString();
    }
}

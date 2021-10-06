package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.FluidGridStack;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidGridStackFactory implements Function<ResourceAmount<FluidResource>, GridStack<FluidResource>> {
    @Override
    public GridStack<FluidResource> apply(ResourceAmount<FluidResource> resourceAmount) {
        FluidVariant fluidVariant = resourceAmount.getResource().getFluidVariant();
        String name = FluidVariantRendering.getName(fluidVariant).getString();
        String modId = Registry.FLUID.getId(fluidVariant.getFluid()).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = FluidTags.getTagGroup().getTagsFor(fluidVariant.getFluid()).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new FluidGridStack(resourceAmount, name, modId, modName, tags);
    }
}

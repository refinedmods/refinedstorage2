package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.FluidGridResourceFactory;

import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class FabricFluidGridResourceFactory extends FluidGridResourceFactory {
    @Override
    protected String getTooltip(final FluidResource resource) {
        return FluidVariantRendering
                .getTooltip(toFluidVariant(resource))
                .stream()
                .map(Component::getString)
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected String getModName(final String modId) {
        return FabricLoader
                .getInstance()
                .getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName)
                .orElse("");
    }

    @Override
    protected String getName(final FluidResource fluidResource) {
        return FluidVariantAttributes.getName(toFluidVariant(fluidResource)).getString();
    }
}

package com.refinedmods.refinedstorage.fabric.grid.view;

import com.refinedmods.refinedstorage.common.grid.view.AbstractFluidGridResourceType;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toFluidVariant;

public class FabricFluidGridResourceType extends AbstractFluidGridResourceType {
    public static final FabricFluidGridResourceType INSTANCE = new FabricFluidGridResourceType();

    private FabricFluidGridResourceType() {
    }

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

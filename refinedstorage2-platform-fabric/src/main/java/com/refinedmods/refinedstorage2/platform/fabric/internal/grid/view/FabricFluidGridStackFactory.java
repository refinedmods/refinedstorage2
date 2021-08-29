package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.FabricFluidGridStack;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricFluidGridStackFactory implements Function<Rs2FluidStack, GridStack<Rs2FluidStack>> {
    @Override
    public GridStack<Rs2FluidStack> apply(Rs2FluidStack stack) {
        FluidVariant fluidVariant = Rs2PlatformApiFacade.INSTANCE.toMcFluid(stack.getFluid());

        String name = stack.getName();
        String modId = Registry.FLUID.getId(fluidVariant.getFluid()).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = FluidTags.getTagGroup().getTagsFor(fluidVariant.getFluid()).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new FabricFluidGridStack(stack, name, modId, modName, tags);
    }
}

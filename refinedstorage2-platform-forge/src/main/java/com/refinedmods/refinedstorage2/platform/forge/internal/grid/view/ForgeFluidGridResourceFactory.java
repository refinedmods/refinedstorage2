package com.refinedmods.refinedstorage2.platform.forge.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.FluidGridResourceFactory;

import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class ForgeFluidGridResourceFactory extends FluidGridResourceFactory {
    @Override
    protected String getTooltip(final FluidResource resource) {
        return getName(resource);
    }

    @Override
    protected String getModName(final String modId) {
        return ModList
                .get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse("");
    }

    @Override
    protected String getName(final FluidResource fluidResource) {
        return toFluidStack(fluidResource, FluidType.BUCKET_VOLUME).getDisplayName().getString();
    }
}

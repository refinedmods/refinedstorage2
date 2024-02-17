package com.refinedmods.refinedstorage2.platform.forge.grid.view;

import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.grid.view.AbstractFluidGridResourceFactory;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidType;

import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidStack;

public class ForgeFluidGridResourceFactory extends AbstractFluidGridResourceFactory {
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

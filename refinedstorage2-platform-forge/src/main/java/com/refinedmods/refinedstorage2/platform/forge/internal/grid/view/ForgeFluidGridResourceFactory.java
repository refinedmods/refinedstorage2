package com.refinedmods.refinedstorage2.platform.forge.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.FluidGridResourceFactory;

import net.minecraftforge.fml.ModList;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class ForgeFluidGridResourceFactory extends FluidGridResourceFactory {
    @Override
    protected String getModName(String modId) {
        return ModList
                .get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse("");
    }

    @Override
    protected String getName(FluidResource fluidResource) {
        return toFluidStack(fluidResource).getDisplayName().getString();
    }
}

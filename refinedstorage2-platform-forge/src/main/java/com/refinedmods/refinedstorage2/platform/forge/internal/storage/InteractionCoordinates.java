package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public interface InteractionCoordinates {
    LazyOptional<IItemHandler> getItemHandler();

    LazyOptional<IFluidHandler> getFluidHandler();
}

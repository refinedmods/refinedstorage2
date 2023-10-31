package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;

import net.minecraft.world.item.ItemStack;

public record ContainedFluid(ItemStack remainderContainer, ResourceAmount<FluidResource> fluid) {
}

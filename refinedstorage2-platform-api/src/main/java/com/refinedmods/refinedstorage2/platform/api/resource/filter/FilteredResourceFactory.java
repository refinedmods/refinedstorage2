package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface FilteredResourceFactory {
    Optional<FilteredResource<?>> create(ItemStack stack, boolean tryAlternatives);
}

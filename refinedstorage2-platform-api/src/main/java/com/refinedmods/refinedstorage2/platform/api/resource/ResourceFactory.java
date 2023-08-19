package com.refinedmods.refinedstorage2.platform.api.resource;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceFactory<T> {
    Optional<ResourceAmountTemplate<T>> create(ItemStack stack);

    boolean isValid(Object resource);
}

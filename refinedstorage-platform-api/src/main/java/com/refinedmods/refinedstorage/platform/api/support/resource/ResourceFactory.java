package com.refinedmods.refinedstorage.platform.api.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceFactory {
    Optional<ResourceAmount> create(ItemStack stack);

    boolean isValid(ResourceKey resource);
}

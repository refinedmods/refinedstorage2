package com.refinedmods.refinedstorage.platform.api.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Defines how a resource can be extracted from a resource slot into a container.
 * For fluids, this maps to how a fluid is inserted into a fluid container like a bucket.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.5")
public interface ResourceContainerInsertStrategy {
    /**
     * @param container      the container to insert the resource into
     * @param resourceAmount the resource to insert into the container
     * @return the result of the insertion, if any. If no result is present, the next insertion strategy will be tried.
     */
    Optional<InsertResult> insert(ItemStack container, ResourceAmount resourceAmount);

    Optional<ConversionInfo> getConversionInfo(ResourceKey resource, ItemStack container);

    record InsertResult(ItemStack container, long inserted) {
    }

    record ConversionInfo(ItemStack from, ItemStack to) {
    }
}

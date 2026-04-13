package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerInsertStrategy;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class FluidResourceContainerInsertStrategy implements ResourceContainerInsertStrategy {
    @Nullable
    private ItemStack emptyBucket;

    private ItemStack getEmptyBucket() {
        if (emptyBucket == null) {
            emptyBucket = new ItemStack(Items.BUCKET);
        }
        return emptyBucket;
    }

    @Override
    public Optional<InsertResult> insert(final ItemStack container, final ResourceAmount resourceAmount) {
        return Platform.INSTANCE.fillContainer(container, resourceAmount).map(
            result -> new InsertResult(result.container(), result.amount())
        );
    }

    @Override
    public Optional<ConversionInfo> getConversionInfo(final ResourceKey resource, final ItemStack carriedStack) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return Optional.empty();
        }
        final ItemStack container = carriedStack.isEmpty() ? getEmptyBucket() : carriedStack;
        final ResourceAmount toFill = new ResourceAmount(fluidResource, Platform.INSTANCE.getBucketAmount());
        return Platform.INSTANCE.fillContainer(container, toFill)
            .filter(result -> result.amount() > 0)
            .map(result -> new ConversionInfo(container, result.container()));
    }
}

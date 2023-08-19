package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import net.minecraft.world.item.ItemStack;

public class ResourceIngredientConverter implements IngredientConverter {
    private final IPlatformFluidHelper<?> fluidHelper;

    public ResourceIngredientConverter(final IPlatformFluidHelper<?> fluidHelper) {
        this.fluidHelper = fluidHelper;
    }

    @Override
    public Optional<ResourceTemplate<?>> convertToResource(final Object ingredient) {
        final var fluid = Platform.INSTANCE.convertJeiIngredientToFluid(ingredient);
        if (fluid.isPresent()) {
            return fluid.map(fluidResource -> new ResourceTemplate<>(
                fluidResource,
                StorageChannelTypes.FLUID
            ));
        }
        if (ingredient instanceof ItemStack itemStack) {
            return Optional.of(new ResourceTemplate<>(
                ItemResource.ofItemStack(itemStack),
                StorageChannelTypes.ITEM
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final Object resource) {
        if (!(resource instanceof ResourceTemplate<?> resourceTemplate)) {
            return Optional.empty();
        }
        if (resourceTemplate.resource() instanceof ItemResource itemResource) {
            return Optional.of(itemResource.toItemStack());
        }
        if (resourceTemplate.resource() instanceof FluidResource fluidResource) {
            return Optional.of(fluidHelper.create(
                fluidResource.fluid(),
                fluidHelper.bucketVolume(),
                fluidResource.tag()
            ));
        }
        return Optional.empty();
    }
}

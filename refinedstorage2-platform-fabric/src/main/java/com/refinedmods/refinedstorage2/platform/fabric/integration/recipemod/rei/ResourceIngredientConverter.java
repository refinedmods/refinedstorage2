package com.refinedmods.refinedstorage2.platform.fabric.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

public class ResourceIngredientConverter implements IngredientConverter {
    @Override
    public Optional<ResourceTemplate<?>> convertToResource(final Object ingredient) {
        if (ingredient instanceof FluidStack fluidStack) {
            return Optional.of(new ResourceTemplate<>(
                new FluidResource(fluidStack.getFluid(), fluidStack.getTag()),
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
            return Optional.of(EntryStacks.of(itemResource.toItemStack()));
        }
        if (resourceTemplate.resource() instanceof FluidResource fluidResource) {
            final FluidStack fluidStack = FluidStack.create(
                fluidResource.fluid(),
                FluidStack.bucketAmount(),
                fluidResource.tag()
            );
            return Optional.of(EntryStacks.of(fluidStack));
        }
        return Optional.empty();
    }
}
